package com.heynaveed.layloscave.utils;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.utils.Array;
import com.heynaveed.layloscave.GameApp;
import com.heynaveed.layloscave.states.PlatformState;
import com.heynaveed.layloscave.universe.Portal;
import com.heynaveed.layloscave.universe.characters.Kirk;
import com.heynaveed.layloscave.universe.platforms.CrumblingPlatform;
import com.heynaveed.layloscave.screens.PlayScreen;

import java.util.ArrayList;
import java.util.Random;

public final class CollisionDetector implements ContactListener {

    private static final Random random = new Random();
    private final Array<Body> bodiesToRemove;
    private final PlayScreen screen;
    private final InputController inputController;
    private final Kirk kirk;

    private static boolean checkForHeadCollision = false;
    private static boolean checkForPortalCollision = false;

    private TiledMap map;

    public CollisionDetector(PlayScreen screen, Kirk kirk){
        super();
        bodiesToRemove = new Array<Body>();
        this.screen = screen;
        inputController = screen.getInputController();
        this.kirk = screen.getKirk();
    }

    @Override
    public void beginContact(Contact contact) {
        Fixture fixA = contact.getFixtureA();
        Fixture fixB = contact.getFixtureB();
        Fixture kirk_body = null;
        Fixture kirk_head = null;
        Fixture diagonal = null;
        Fixture foreignObject = null;
        checkForPortalCollision = false;

        int cDef = fixA.getFilterData().categoryBits | fixB.getFilterData().categoryBits;

        if(fixA.getUserData() == "kirk_body" || fixB.getUserData() == "kirk_body"){
            kirk_body = fixA.getUserData() == "kirk_body" ? fixA : fixB;
            foreignObject = kirk_body == fixA ? fixB : fixA;
        }
        if(fixA.getUserData() == "kirk_head" || fixB.getUserData() == "kirk_head"){
            kirk_body = fixA.getUserData() == "kirk_head" ? fixA : fixB;
            foreignObject = kirk_body == fixA ? fixB : fixA;
            checkForPortalCollision = true;
        }



        switch(cDef){
            case GameApp.KIRK_BIT | GameApp.MUDDY_PLATFORM_BIT:
                resetKirkStates(false);
                kirk.setCurrentPlatformState(PlatformState.MUDDY);
                break;
            case GameApp.KIRK_BIT | GameApp.ICE_PLATFORM_BIT:
                resetKirkStates(false);
                kirk.setCurrentPlatformState(PlatformState.ICE);
                if(Math.abs(kirk.getBody().getLinearVelocity().y) > 0)
                    kirk.setSliding(true);
                break;
            case GameApp.KIRK_BIT | GameApp.GROUND_PLATFORM_BIT:
                resetKirkStates(false);
                kirk.setCurrentPlatformState(PlatformState.GROUND);
                break;
            case GameApp.KIRK_BIT | GameApp.BOUNCY_PLATFORM_BIT:
                resetKirkStates(true);
                kirk.setCurrentPlatformState(PlatformState.BOUNCY);
                break;
            case GameApp.KIRK_BIT | GameApp.CRUMBLING_WALL_BIT:
                resetKirkStates(false);
                kirk.setCurrentPlatformState(PlatformState.CRUMBLING);
                ((CrumblingPlatform) foreignObject.getUserData()).setIsTouched(true);
                bodiesToRemove.add(foreignObject.getBody());
                break;
            case GameApp.KIRK_BIT | GameApp.PORTAL_BIT:
                if(checkForPortalCollision && !kirk.isPortalLocked()){
                    ArrayList<Portal> portals = screen.getPortals();
                    Portal partnerPortal = null;
                    for(int i = 0; i < portals.size(); i++){

                        if(foreignObject.getUserData().equals(Portal.RANDOM_PORTAL_BIT_ONE)) {
                            partnerPortal = portals.get(random.nextInt(Portal.MAX_PORTAL_NUMBER));
                            kirk.setSourcePortal(portals.get(10));
                        }
                        else if(foreignObject.getUserData().equals(Portal.RANDOM_PORTAL_BIT_TWO)){
                            partnerPortal = portals.get(random.nextInt(Portal.MAX_PORTAL_NUMBER));
                            kirk.setSourcePortal(portals.get(11));
                        }
                        else if(foreignObject.getUserData().equals(portals.get(i).getId())) {
                            partnerPortal = portals.get(portals.get(i).getPartnerId());
                            kirk.setSourcePortal(portals.get(i));
                        }
                        else continue;

                        kirk.setPortalLocked(true);
                        kirk.setTargetPortal(partnerPortal);
                        break;
                    }
                    kirk.setControlDisabled(true);
                }
                break;
        }
    }

    @Override
    public void endContact(Contact contact) {
        Fixture fixA = contact.getFixtureA();
        Fixture fixB = contact.getFixtureB();

        int cDef = fixA.getFilterData().categoryBits | fixB.getFilterData().categoryBits;

        switch(cDef){
            case GameApp.KIRK_BIT | GameApp.GROUND_PLATFORM_BIT:
                kirk.setControlDisabled(false);
                kirk.setCurrentPlatformState(PlatformState.NONE);
                break;
            case GameApp.KIRK_BIT | GameApp.MUDDY_PLATFORM_BIT:
                kirk.setCurrentPlatformState(PlatformState.NONE);
                break;
            case GameApp.KIRK_BIT | GameApp.ICE_PLATFORM_BIT:
                kirk.setCurrentPlatformState(PlatformState.NONE);
                break;
            case GameApp.KIRK_BIT | GameApp.BOUNCY_PLATFORM_BIT:
                kirk.setControlDisabled(false);
                kirk.setCurrentPlatformState(PlatformState.NONE);
                kirk.resetBounceTimer();
                break;
            case GameApp.KIRK_BIT | GameApp.CRUMBLING_WALL_BIT:
                kirk.setControlDisabled(false);
                kirk.setCurrentPlatformState(PlatformState.NONE);
                break;
        }
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
        Fixture fixA = contact.getFixtureA();
        Fixture fixB = contact.getFixtureB();
        checkForHeadCollision = false;

        int cDef = fixA.getFilterData().categoryBits | fixB.getFilterData().categoryBits;

        if(fixA.getUserData() == "kirk_head" || fixB.getUserData() == "kirk_head")
            checkForHeadCollision = true;

        switch(cDef){
            case GameApp.KIRK_BIT | GameApp.GROUND_PLATFORM_BIT:
                kirk.setCurrentPlatformState(PlatformState.GROUND);
//                if(checkForHeadCollision)
//                    System.out.println("HEAD COLLISION");
                break;
            case GameApp.KIRK_BIT | GameApp.MUDDY_PLATFORM_BIT:
                kirk.setCurrentPlatformState(PlatformState.MUDDY);
                break;
            case GameApp.KIRK_BIT | GameApp.ICE_PLATFORM_BIT:
                kirk.setCurrentPlatformState(PlatformState.ICE);
                break;
            case GameApp.KIRK_BIT | GameApp.BOUNCY_PLATFORM_BIT:
                kirk.setCurrentPlatformState(PlatformState.BOUNCY);
                break;
            case GameApp.KIRK_BIT | GameApp.CRUMBLING_WALL_BIT:
                kirk.setCurrentPlatformState(PlatformState.CRUMBLING);
                break;
        }
    }

    private void resetKirkStates(boolean isDisablingPlatform){
        kirk.setSliding(false);
        kirk.setStraightJumping(false);
        kirk.setBounceJump(false);
        kirk.setControlDisabled(isDisablingPlatform);
        inputController.setBounceJumpImpulse(false);
        inputController.setDoubleJumpImpulse(false);
        inputController.setIceBurstImpulse(false);
    }

    public Array<Body> getBodiesToRemove(){
        return bodiesToRemove;
    }

    public void setMap(TiledMap map){
        this.map = map;
    }
}
