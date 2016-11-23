package com.heynaveed.layloscave.universe;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.heynaveed.layloscave.GameApp;


public abstract class Platform {

    private static final float NORMAL_FRICTION = 12.0f;

    protected final World world;
    protected final BodyDef bodyDef;
    protected final FixtureDef boxFixtureDef;
    protected final FixtureDef floorFixtureDef;
    protected final FixtureDef leftFixtureDef;
    protected final FixtureDef rightFixtureDef;
    protected final MapObject object;
    protected final Rectangle rect;
    protected final Body body;
    protected final Vector2 position;
    protected float width;
    protected float height;

    public Platform(World world, MapObject object){
        this.world = world;
        this.object = object;
        this.rect = ((RectangleMapObject) object).getRectangle();
        bodyDef = new BodyDef();
        body = instantiateBody(bodyDef);
        boxFixtureDef = new FixtureDef();
        floorFixtureDef = new FixtureDef();
        leftFixtureDef = new FixtureDef();
        rightFixtureDef = new FixtureDef();
        position = new Vector2(rect.getX(), rect.getY());
        width = rect.getWidth();
        height = rect.getHeight();
        floorFixtureDef.friction = NORMAL_FRICTION;
    }

    private Body instantiateBody(BodyDef bDef){
        bodyDef.type = BodyDef.BodyType.KinematicBody;
        bodyDef.position.set(GameApp.toPPM(rect.getX() + rect.getWidth()/2), GameApp.toPPM(rect.getY() + rect.getHeight()/2));
        return world.createBody(bodyDef);
    }

    protected int calculateOffset(int length){
        if(length % 2 == 0)
            return 32;
        else
            return 0;
    }

    protected void createPlatform(short categoryBit){
        PolygonShape boxShape = new PolygonShape();
        boxShape.setAsBox(GameApp.toPPM(rect.getWidth()/2), GameApp.toPPM(rect.getHeight()/2));
        boxFixtureDef.shape = boxShape;
        boxFixtureDef.filter.categoryBits = GameApp.GROUND_PLATFORM_BIT;
        boxFixtureDef.friction = 0;
        body.createFixture(boxFixtureDef).setUserData("boxShape");

        EdgeShape top = new EdgeShape();
        top.set(new Vector2(GameApp.toPPM(-rect.getWidth()/2), GameApp.toPPM(rect.getHeight()+3)/2), new Vector2(GameApp.toPPM(rect.getWidth()/2), GameApp.toPPM(rect.getHeight()+3)/2));
        floorFixtureDef.shape = top;
        floorFixtureDef.filter.categoryBits = categoryBit;
        body.createFixture(floorFixtureDef).setUserData(this);

        EdgeShape left = new EdgeShape();
        left.set(new Vector2((GameApp.toPPM(-rect.getWidth()-2)/2), GameApp.toPPM(rect.getHeight()-3)/2), new Vector2((GameApp.toPPM(-rect.getWidth()-2)/2), GameApp.toPPM(-rect.getHeight()-3)/2));
        leftFixtureDef.shape = left;
        leftFixtureDef.filter.categoryBits = GameApp.SIDE_WALL_BIT;
        leftFixtureDef.friction = 0;
        leftFixtureDef.restitution = 0;
        body.createFixture(leftFixtureDef).setUserData("leftPlatform");

        EdgeShape right = new EdgeShape();
        right.set(new Vector2((GameApp.toPPM(rect.getWidth()+2)/2), GameApp.toPPM(rect.getHeight()-3)/2), new Vector2((GameApp.toPPM(rect.getWidth()+2)/2), GameApp.toPPM(-rect.getHeight()-3)/2));
        rightFixtureDef.shape = right;
        rightFixtureDef.filter.categoryBits = GameApp.SIDE_WALL_BIT;
        rightFixtureDef.friction = 0;
        rightFixtureDef.restitution = 0;
        body.createFixture(rightFixtureDef).setUserData("rightPlatform");

        body.setUserData(this);
    }

    protected abstract void update(float delta);
}
