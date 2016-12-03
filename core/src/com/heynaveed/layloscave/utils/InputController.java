package com.heynaveed.layloscave.utils;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.heynaveed.layloscave.GameApp;
import com.heynaveed.layloscave.screens.PlayScreen;
import com.heynaveed.layloscave.keys.ControlKey;
import com.heynaveed.layloscave.states.CharacterState;
import com.heynaveed.layloscave.states.PlatformState;
import com.heynaveed.layloscave.universe.characters.Jini;
import com.heynaveed.layloscave.universe.characters.Kirk;

public final class InputController implements InputProcessor {

    private final boolean[] controls = new boolean[ControlKey.values().length];

    private final Stage stage;
    private final Skin kirkSkin, jiniSkin, leftSkin, rightSkin;
    private ImageButton kirkButton, jiniButton, leftButton, rightButton;
    private final ImageButtonStyle kirkButtonStyle, jiniButtonStyle, leftButtonStyle, rightButtonStyle;
    private final TextureAtlas kirkAtlas, jiniAtlas, leftAtlas, rightAtlas;
    private final PlayScreen screen;
    private Kirk kirk;
    private Jini jini;

    private Vector2 dragStart;
    private boolean bounceJumpImpulse;
    private boolean doubleJumpImpulse;
    private boolean iceBurstImpulse;

    public InputController(PlayScreen screen){
        this.screen = screen;
        stage = new Stage();
        kirkSkin = new Skin();
        jiniSkin = new Skin();
        leftSkin = new Skin();
        rightSkin = new Skin();
        kirkAtlas = new TextureAtlas(Gdx.files.internal("sprite-sheets/kirkButton.atlas"));
        jiniAtlas = new TextureAtlas(Gdx.files.internal("sprite-sheets/jiniButton.atlas"));
        leftAtlas = new TextureAtlas(Gdx.files.internal("sprite-sheets/leftButton.atlas"));
        rightAtlas = new TextureAtlas(Gdx.files.internal("sprite-sheets/rightButton.atlas"));
        kirkButtonStyle = new ImageButtonStyle();
        jiniButtonStyle = new ImageButtonStyle();
        leftButtonStyle = new ImageButtonStyle();
        rightButtonStyle = new ImageButtonStyle();
        bounceJumpImpulse = false;
        doubleJumpImpulse = false;
        iceBurstImpulse = false;

        if(GameApp.CONFIGURATION == "Android")
            initialiseButtons();

        initialiseControls();
    }

    public void update(float dt){

        for(int i = 0; i < ControlKey.values().length; i++) {
            if(controls[ControlKey.values()[i].index])
                applyInputControl(ControlKey.values()[i]);
        }

        kirk.update(dt);
        jini.update(dt);
    }

    private void initialiseButtons(){
        kirkSkin.addRegions(kirkAtlas);
        jiniSkin.addRegions(jiniAtlas);
        leftSkin.addRegions(leftAtlas);
        rightSkin.addRegions(rightAtlas);
        kirkButtonStyle.up = kirkSkin.getDrawable("controlButtonsUnpressed");
        kirkButtonStyle.down = kirkSkin.getDrawable("controlButtonsPressed");
        jiniButtonStyle.up = jiniSkin.getDrawable("controlButtonsUnpressed");
        jiniButtonStyle.down = jiniSkin.getDrawable("controlButtonsPressed");
        leftButtonStyle.up = leftSkin.getDrawable("controlButtonsUnpressed");
        leftButtonStyle.down = leftSkin.getDrawable("controlButtonsPressed");
        rightButtonStyle.up = rightSkin.getDrawable("controlButtonsUnpressed");
        rightButtonStyle.down = rightSkin.getDrawable("controlButtonsPressed");
        kirkButton = new ImageButton(kirkButtonStyle);
        jiniButton = new ImageButton(jiniButtonStyle);
        leftButton = new ImageButton(leftButtonStyle);
        rightButton = new ImageButton(rightButtonStyle);
        kirkButton.setWidth(Gdx.graphics.getWidth()/9);
        kirkButton.setHeight(Gdx.graphics.getHeight()/5.5f);
        jiniButton.setWidth(Gdx.graphics.getWidth()/9);
        jiniButton.setHeight(Gdx.graphics.getHeight()/5.5f);
        leftButton.setWidth(Gdx.graphics.getWidth()/9.0f);
        leftButton.setHeight(Gdx.graphics.getHeight()/7.0f);
        rightButton.setWidth(Gdx.graphics.getWidth()/9.0f);
        rightButton.setHeight(Gdx.graphics.getHeight()/7.0f);
        kirkButton.setPosition(Gdx.graphics.getWidth() - (Gdx.graphics.getWidth()/9)*2.5f, (Gdx.graphics.getHeight()/5.5f)*0.1f);
        jiniButton.setPosition(Gdx.graphics.getWidth() - (Gdx.graphics.getWidth()/9)*1.5f, (Gdx.graphics.getHeight()/5.5f)*0.1f);
        leftButton.setPosition((Gdx.graphics.getWidth()/20.0f)*1.05f, (Gdx.graphics.getHeight()/5.5f)*0.2f);
        rightButton.setPosition((Gdx.graphics.getWidth()/20.0f)*3.6f, (Gdx.graphics.getHeight()/5.5f)*0.2f);
        kirkButton.setChecked(false);
        jiniButton.setChecked(false);
        leftButton.setChecked(false);
        rightButton.setChecked(false);

        stage.addActor(kirkButton);
        stage.addActor(jiniButton);
        stage.addActor(leftButton);
        stage.addActor(rightButton);

        kirkButton.addListener(new ClickListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button){
                if(kirk.getCurrentPlatformState() != PlatformState.BOUNCY && !kirk.isSliding())
                    kirk.jump();
                return true;
            }

            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                controls[ControlKey.UP.index] = false;
            }
        });

        jiniButton.addListener(new ClickListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button){
                if(kirk.getCurrentPlatformState() == PlatformState.ICE && !kirk.isSliding())
                    applyJiniImpulse(CharacterState.Jini.ICE_BURST_IMPULSE);
                else if(kirk.getCurrentPlatformState() == PlatformState.NONE && !doubleJumpImpulse)
                    applyJiniImpulse(CharacterState.Jini.DOUBLE_JUMP);
                else if(kirk.isBounceJumping())
                    applyJiniImpulse(CharacterState.Jini.BOUNCE_IMPULSE);
                return true;
            }

            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
            }
        });

        leftButton.addListener(new ClickListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button){
                controls[ControlKey.LEFT.index] = true;

                if(!jini.isTeleporting() && kirk.isFacingRight()) {
                    jini.setIsTeleporting(true);
                    jini.resetAnimationStateTimer();
                }

                return true;
            }

            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                if(kirk.getCurrentPlatformState() == PlatformState.ICE && !kirk.isSliding())
                    kirk.applySlide();
                controls[ControlKey.LEFT.index] = false;
            }
        });

        rightButton.addListener(new ClickListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button){
                controls[ControlKey.RIGHT.index] = true;

                if(!jini.isTeleporting() && !kirk.isFacingRight()) {
                    jini.setIsTeleporting(true);
                    jini.resetAnimationStateTimer();
                }
                return true;
            }

            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                if(kirk.getCurrentPlatformState() == PlatformState.ICE && !kirk.isSliding())
                    kirk.applySlide();
                controls[ControlKey.RIGHT.index] = false;
            }
        });
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
                controls[ControlKey.LEFT.index] = true;

                if(!jini.isTeleporting() && kirk.isFacingRight()) {
                    jini.setIsTeleporting(true);
                    jini.resetAnimationStateTimer();
                }
                return true;
            case Input.Keys.RIGHT:
                controls[ControlKey.RIGHT.index] = true;

                if(!jini.isTeleporting() && !kirk.isFacingRight()) {
                    jini.setIsTeleporting(true);
                    jini.resetAnimationStateTimer();
                }
                return true;
            case Input.Keys.UP:
                if(kirk.getCurrentPlatformState() != PlatformState.BOUNCY && !kirk.isSliding())
                    kirk.jump();
                return true;
            case Input.Keys.DOWN:
                controls[ControlKey.DOWN.index] = true;
                return true;
            case Input.Keys.SPACE:
                if(kirk.getCurrentPlatformState() == PlatformState.ICE && !kirk.isSliding())
                    applyJiniImpulse(CharacterState.Jini.ICE_BURST_IMPULSE);
                else if(kirk.getCurrentPlatformState() == PlatformState.NONE && !doubleJumpImpulse)
                    applyJiniImpulse(CharacterState.Jini.DOUBLE_JUMP);
                else if(kirk.isBounceJumping())
                    applyJiniImpulse(CharacterState.Jini.BOUNCE_IMPULSE);
                return true;
        }

        return false;
    }

    @Override
    public boolean keyUp(int keycode)  {
        switch(keycode){
            case Input.Keys.LEFT:
                if(kirk.getCurrentPlatformState() == PlatformState.ICE && !kirk.isSliding())
                    kirk.applySlide();
                controls[ControlKey.LEFT.index] = false;
                return true;
            case Input.Keys.RIGHT:
                if(kirk.getCurrentPlatformState() == PlatformState.ICE && !kirk.isSliding())
                    kirk.applySlide();
                controls[ControlKey.RIGHT.index] = false;
                return true;
            case Input.Keys.UP:
                controls[ControlKey.UP.index] = false;
                return true;
            case Input.Keys.DOWN:
                controls[ControlKey.DOWN.index] = false;
                return true;
            case Input.Keys.SPACE:
                return true;
        }

        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        dragStart = new Vector2(screenX, screenY);
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {

        if(controls[ControlKey.DOWN.index]){
            controls[ControlKey.DOWN.index] = false;
            return true;
        }

        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {

        if(dragStart.y - screenY < 0){
            controls[ControlKey.DOWN.index] = true;
            return true;
        }

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

    public void dispose(){
        screen.dispose();
        stage.dispose();
        jiniSkin.dispose();
        jiniAtlas.dispose();
    }

    public Stage getStage(){
        return stage;
    }
}
