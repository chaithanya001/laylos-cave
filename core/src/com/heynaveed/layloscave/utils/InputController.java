package com.heynaveed.layloscave.utils;


import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;
import com.heynaveed.layloscave.GameApp;
import com.heynaveed.layloscave.screens.PlayScreen;
import com.heynaveed.layloscave.keys.ControlKey;
import com.heynaveed.layloscave.states.CharacterState;
import com.heynaveed.layloscave.states.PlatformState;
import com.heynaveed.layloscave.universe.characters.Jini;
import com.heynaveed.layloscave.universe.characters.Kirk;

public final class InputController implements InputProcessor {

    private final boolean[] controls = new boolean[ControlKey.values().length];

    private final PlayScreen screen;
    private Kirk kirk;
    private Jini jini;

    private boolean bounceJumpImpulse;
    private boolean doubleJumpImpulse;
    private boolean iceBurstImpulse;

    public InputController(PlayScreen screen){
        this.screen = screen;
        bounceJumpImpulse = false;
        doubleJumpImpulse = false;
        iceBurstImpulse = false;
        initialiseControls();
    }

    public void update(float dt){

        for(int i = 0; i < ControlKey.values().length; i++) {
            if(controls[ControlKey.values()[i].getKey()])
                applyInputControl(ControlKey.values()[i]);
        }

        kirk.update(dt);
        jini.update(dt);
    }

    private void initialiseControls(){
        for (int i = 0; i < controls.length; i++)
            controls[i] = false;
    }

    private void applyInputControl(ControlKey key){

        if(!kirk.isControlDisabled() && key != ControlKey.UP) {
            switch (key) {
                case LEFT:
                    if(kirk.isAllowedToRun()) {
                        kirk.setFacingRight(false);
                        kirk.applyRunImpulse();
                    }
                    break;
                case RIGHT:
                    if(kirk.isAllowedToRun()) {
                        kirk.setFacingRight(true);
                        kirk.applyRunImpulse();
                    }
                    break;
                case DOWN:
                    if(kirk.getCurrentPlatformState() == PlatformState.NONE)
                        kirk.glideDown();
                    break;
            }
        }
    }

    private void applyJiniImpulse(CharacterState.Jini state) {

        kirk.getBody().setLinearVelocity(0, 0);

        switch(state){
            case DOUBLE_JUMP:
                kirk.getBody().applyLinearImpulse(new Vector2(kirk.horizontalDirectionMultiplyer(5.0f), 22.5f), kirk.getBody().getWorldCenter(), true);
                doubleJumpImpulse = true;
                jini.setisLevitateImpulse(true);
                break;
            case BOUNCE_IMPULSE:
                kirk.getBody().setLinearVelocity(new Vector2(0, -5.0f));
                bounceJumpImpulse = true;
                break;
            case ICE_BURST_IMPULSE:
                kirk.getBody().applyLinearImpulse(new Vector2(kirk.horizontalDirectionMultiplyer(100.0f), 2.5f), kirk.getBody().getWorldCenter(), true);
                iceBurstImpulse = true;
                break;
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        switch(keycode){
            case Input.Keys.LEFT:
                controls[ControlKey.LEFT.getKey()] = true;

                if(!jini.isTeleporting() && kirk.isFacingRight()) {
                    jini.setIsTeleporting(true);
                    jini.resetAnimationStateTimer();
                }
                break;
            case Input.Keys.RIGHT:
                controls[ControlKey.RIGHT.getKey()] = true;

                if(!jini.isTeleporting() && !kirk.isFacingRight()) {
                    jini.setIsTeleporting(true);
                    jini.resetAnimationStateTimer();
                }
                break;
            case Input.Keys.UP:
                if(kirk.getCurrentPlatformState() != PlatformState.BOUNCY && !kirk.isSliding())
                    kirk.jump();
                break;
            case Input.Keys.DOWN:
                controls[ControlKey.DOWN.getKey()] = true;
                break;
            case Input.Keys.SPACE:
                if(kirk.getCurrentPlatformState() == PlatformState.ICE && !kirk.isSliding())
                    applyJiniImpulse(CharacterState.Jini.ICE_BURST_IMPULSE);
                else if(kirk.getCurrentPlatformState() == PlatformState.NONE && !doubleJumpImpulse)
                    applyJiniImpulse(CharacterState.Jini.DOUBLE_JUMP);
                else if(kirk.isBounceJumping())
                    applyJiniImpulse(CharacterState.Jini.BOUNCE_IMPULSE);
                break;
        }

        return true;
    }

    @Override
    public boolean keyUp(int keycode)  {
        switch(keycode){
            case Input.Keys.LEFT:
                if(kirk.getCurrentPlatformState() == PlatformState.ICE && !kirk.isSliding())
                    kirk.applySlide();
                controls[ControlKey.LEFT.getKey()] = false;
                break;
            case Input.Keys.RIGHT:
                if(kirk.getCurrentPlatformState() == PlatformState.ICE && !kirk.isSliding())
                    kirk.applySlide();
                controls[ControlKey.RIGHT.getKey()] = false;
                break;
            case Input.Keys.UP:
                controls[ControlKey.UP.getKey()] = false;
                break;
            case Input.Keys.DOWN:
                controls[ControlKey.DOWN.getKey()] = false;
                break;
            case Input.Keys.SPACE:
                break;
        }

        return true;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if(screenX < GameApp.WIDTH/2) {
            controls[ControlKey.LEFT.getKey()] = true;

            if(!jini.isTeleporting() && kirk.isFacingRight()) {
                jini.setIsTeleporting(true);
                jini.resetAnimationStateTimer();
            }
        }
        else {
            controls[ControlKey.RIGHT.getKey()] = true;

            if(!jini.isTeleporting() && !kirk.isFacingRight()) {
                jini.setIsTeleporting(true);
                jini.resetAnimationStateTimer();
            }
        }

        if(screenY < GameApp.HEIGHT && kirk.getCurrentPlatformState() != PlatformState.BOUNCY
                && !kirk.isSliding() && kirk.getBody().getLinearVelocity().y == 0) {
            kirk.jump();
        }
        else if(kirk.getCurrentPlatformState() == PlatformState.NONE && !doubleJumpImpulse)
            applyJiniImpulse(CharacterState.Jini.DOUBLE_JUMP);

        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if(screenX < GameApp.WIDTH/2) {
            if(kirk.getCurrentPlatformState() == PlatformState.ICE)
                kirk.applySlide();
            controls[ControlKey.LEFT.getKey()] = false;
        }
        else {
            if(kirk.getCurrentPlatformState() == PlatformState.ICE)
                kirk.applySlide();
            controls[ControlKey.RIGHT.getKey()] = false;
        }

        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

    public boolean[] getControls(){
        return controls;
    }

    public boolean getDoubleJumpImpulse(){
        return doubleJumpImpulse;
    }

    public boolean getBounceJumpImpulse(){
        return bounceJumpImpulse;
    }

    public void setDoubleJumpImpulse(boolean doubleJumpImpulse){
        this.doubleJumpImpulse = doubleJumpImpulse;
    }

    public void setBounceJumpImpulse(boolean bounceJumpImpulse){
        this.bounceJumpImpulse = bounceJumpImpulse;
    }

    public void setIceBurstImpulse(boolean iceBurstImpulse){
        this.iceBurstImpulse = iceBurstImpulse;
    }

    public InputController setKirk(Kirk kirk){
        this.kirk = kirk;
        return this;
    }

    public InputController setJini(Jini jini){
        this.jini = jini;
        return this;
    }

    public boolean getIceBurstImpulse() {
        return iceBurstImpulse;
    }
}
