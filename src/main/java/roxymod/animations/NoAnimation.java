package roxymod.animations;

import basemod.animations.AbstractAnimation;

public class NoAnimation extends AbstractAnimation {
    @Override
    public Type type() {
        return Type.NONE;
    }
}

