import fs from 'node:fs/promises';
import {createRequire} from 'node:module';
import {pathToFileURL} from 'node:url';
const runtime = 'C:/Users/13174/.cache/codex-runtimes/codex-primary-runtime/dependencies/node';
const require = createRequire(`${runtime}/package.json`);
const {FileBlob, SpreadsheetFile} = await import(pathToFileURL(require.resolve('@oai/artifact-tool')).href);
const root = 'C:/Users/13174/Desktop/mod';
const strings = JSON.parse(await fs.readFile(`${root}/src/main/resources/startermod/localization/zhs/CardStrings.json`,'utf8'));
const changed = new Set(['RoxyWaterNeedle','RoxyCrystalShard','RoxyBlueCrystal','RoxyRapidChant','RoxyDrippingFocus','RoxyRecast','RoxyFlowBarrier','RoxyFlowingCast','RoxyCrystalWard','RoxyBlizzardStorm','RoxyCrystalDomain','RoxyCrystalStaff','RoxyLightning','RoxyGreatFlood']);
const clean = text => text.replace(/ NL /g,'\n').replace(/ +([。，：；])/g,'$1');
const updates = new Map();
for (const id of changed) {
  const src = await fs.readFile(`${root}/src/main/java/startermod/cards/${id}.java`,'utf8');
  const constants = Object.fromEntries([...src.matchAll(/private static final int (\w+) = (-?\d+);/g)].map(m=>[m[1],Number(m[2])]));
  const value = token => token === undefined ? 0 : /^-?\d+$/.test(token) ? Number(token) : constants[token];
  const stats = Object.fromEntries(['Damage','Block','MagicNumber'].map(k=>[k,value(src.match(new RegExp(`this\\.base${k} = ([A-Z_0-9]+);`))?.[1])]));
  const upgraded = Object.fromEntries(Object.entries(stats).map(([k,v])=>[k,v + value(src.match(new RegExp(`this\\.upgrade${k}\\(([^)]+)\\)`))?.[1])]));
  const loc = strings[`startermod:${id}`];
  const substitute = (text,s) => clean(text.replace(/!D!/g,s.Damage).replace(/!B!/g,s.Block).replace(/!M!/g,s.MagicNumber));
  const cost = constants.COST;
  const costToken = src.match(/this\.upgradeBaseCost\(([^)]+)\)/)?.[1];
  const upgradeCost = costToken === undefined ? cost : value(costToken);
  const changes = Object.entries(stats).filter(([k,v])=>v!==upgraded[k]).map(([k,v])=>`${k} ${v} → ${upgraded[k]}`);
  if (cost !== upgradeCost) changes.push(`费用 ${cost} → ${upgradeCost}`);
  updates.set(id,[cost,upgradeCost,substitute(loc.DESCRIPTION,stats),substitute(loc.UPGRADE_DESCRIPTION??loc.DESCRIPTION,upgraded),changes.join('；')||'效果说明已同步',clean(loc.DESCRIPTION),clean(loc.UPGRADE_DESCRIPTION??loc.DESCRIPTION)]);
}
for (const name of ['roxy_card_overview','roxy_content_overview']) {
  const path = `${root}/outputs/${name}/${name}.xlsx`;
  const wb = await SpreadsheetFile.importXlsx(await FileBlob.load(path));
  const sheet = wb.worksheets.getItemAt(0);
  const rows = sheet.getRange('A4:P78').values;
  const modified=[];
  for (let i=0;i<rows.length;i++) {
    const id = rows[i][1];
    if (!updates.has(id)) continue;
    const row = i+4, data=updates.get(id);
    sheet.getRange(`I${row}:O${row}`).values=[data];
    sheet.getRange(`A${row}:P${row}`).format.rowHeight = Math.max(64,Math.max(data[2].split('\n').length,data[3].split('\n').length)*18+16);
    modified.push({id,row});
  }
  if (modified.length !== changed.size) throw new Error('Missing overview updates');
  sheet.getRange('A2').values=[[sheet.getRange('A2').values[0][0]+' 更新：2026-10-23（0.2.0）。']];
  wb.recalculate();
  const errors=await wb.inspect({kind:'match',searchTerm:'#REF!|#DIV/0!|#VALUE!|#NAME\\?|#NUM!',options:{useRegex:true,maxResults:20},maxChars:1000});
  console.log(name,errors.ndjson);
  for (const item of modified) {
    const actual=sheet.getRange(`I${item.row}:O${item.row}`).values[0];
    if (JSON.stringify(actual)!==JSON.stringify(updates.get(item.id))) throw new Error(`Update mismatch: ${item.id}`);
  }
  for (const id of ['RoxyBlueCrystal','RoxyLightning']) {
    const {row}=modified.find(m=>m.id===id);
    const preview=await wb.render({sheetName:sheet.name,range:`I${row}:O${row}`,scale:1,format:'png'});
    await fs.writeFile(`${root}/outputs/polish_20261023/${name}-${id}-after.png`,new Uint8Array(await preview.arrayBuffer()));
  }
  await (await SpreadsheetFile.exportXlsx(wb)).save(path);
  console.log(`Updated and verified ${modified.length} rows: ${name}`);
}
