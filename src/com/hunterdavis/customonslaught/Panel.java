package com.hunterdavis.customonslaught;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

class Panel extends SurfaceView implements SurfaceHolder.Callback {

	InventorySQLHelper scoreData = null;

	// member variables
	private CanvasThread canvasthread;
	public Boolean surfaceCreated;
	public Context mContext;
	int difficulty = 0;
	public Boolean gameOver = false;
	public Boolean gamePaused = false;
	public Boolean introScreenOver = false;
	Point player1Pos = new Point(25, 25);
	Point player1Wants = new Point(0, 0);
	public Bitmap cometBitmap = null;
	public Bitmap cometBitmapLarge = null;
	public Bitmap cometBitmapSmall = null;
	public Bitmap player1Bitmap = null;
	public Bitmap player1IconBitmap = null;
	public Uri selectedImageUri = null;
	public Uri selectedPlayerUri = null;
	String playerName = null;
	int mwidth = 0;
	int mheight = 0;
	int player1Score = 0;
	int cometSize = 0;
	int player1Lives = 0;
	int player1Health = 0;
	int enemiesKilled = 0;
	int enemiesPerLevel = 0;
	int enemiesKilledThisLevel = 0;
	int level = 1;
	int enemyDamage = 0;
	int numberOfEnemiesAllowed = 0;
	int numberOfBulletsAllowed = 0;
	int weaponLevel = 0;
	Random myrandom = new Random();
	List<Enemy> enemyList;
	List<Bullet> bulletList;
	List<bloodPoint> bloodList;

	// tweaking for game mechanics
	int enemiesPerLevelConstant = 6;
	int numberOfBloodSpots = 5000;
	int maxDepthValue = 5;
	int player1Step = 2;
	int player1IconSize = 12;
	int bloodTTL = 10;
	int bulletStep = 10;
	int player1Size = 5;
	int emenyStartDamage = 4;
	int player1StartingHealth = 40; 
	int boomStickHurtValue = 50;
	int playerColor = Color.rgb(204, 8, 57);
	int scoreColor = Color.rgb(0, 0, 234);
	int healthBarColor = Color.rgb(255, 35, 35);
	int enemyHealthColor = Color.rgb(255, 65, 65);
	int enemyBloodColor = Color.rgb(255, 65, 65);
	int bulletColor = Color.rgb(47, 79, 79);
	int player1LivesStarting = 3;
	int maxBulletSize = 25;
	int initialNumberOfEnemies = 3;
	int numCracks = 3;
	Boolean shootReverse = false;
	private static final float EPS = (float) 0.000001;

	public class Enemy {
		public int x;
		public int y;
		public int size;
		public int healthPoints;
		public Boolean left;
		public Boolean down;

		Enemy(int xa, int ya, int sizea, int healtha, Boolean lefta,
				Boolean downa) {
			x = xa;
			y = ya;
			size = sizea;
			healthPoints = healtha;
			left = lefta;
			down = downa;
		}

		Enemy(int xa, int ya) {
			x = xa;
			y = ya;
			size = myrandom.nextInt(3);
			healthPoints = (int) Math.pow(2, size);
			left = myrandom.nextBoolean();
			down = myrandom.nextBoolean();

		}

		Enemy(Enemy newComet) {
			x = newComet.x;
			y = newComet.y;
			size = newComet.size;
			healthPoints = newComet.healthPoints;
			left = newComet.left;
			down = newComet.down;
		}
	}

	public class Bullet {
		public int x;
		public int y;
		public int xdest;
		public int ydest;

		Bullet(int xa, int ya, int xdesta, int ydesta) {
			x = xa;
			y = ya;
			xdest = xdesta;
			ydest = ydesta;
		}

		Bullet(Bullet tempBull) {
			x = tempBull.x;
			y = tempBull.y;
			xdest = tempBull.xdest;
			ydest = tempBull.ydest;
		}
	}

	public class bloodPoint {
		public int x;
		public int y;
		public int age;

		bloodPoint(int xa, int ya, int agea) {
			x = xa;
			y = ya;
			age = agea;
		}

		bloodPoint(bloodPoint b) {
			x = b.x;
			y = b.y;
			age = b.age;

		}

		bloodPoint() {
			x = myrandom.nextInt(mwidth);
			y = myrandom.nextInt(mheight);
			age = 0;
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		synchronized (getHolder()) {

			int action = event.getAction();
			if (action == MotionEvent.ACTION_DOWN) {

				player1Wants.x = (int) event.getX();
				player1Wants.y = (int) event.getY();

				if (gamePaused == true) {
					gamePaused = false;
				}

				addBullet();

				return true;
			} else if (action == MotionEvent.ACTION_MOVE) {

				player1Wants.x = (int) event.getX();
				player1Wants.y = (int) event.getY();
				addBullet();

				return true;
			} else if (action == MotionEvent.ACTION_UP) {

				return true;
			}
			return true;
		}
	}

	public void setDifficulty(int difficult) {
		difficulty = difficult;
		initialNumberOfEnemies = (difficulty + 2) * 2;
		maxBulletSize = 25 + (5 * difficult);
		reset();
	}

	public void setUri(Uri uri) {
		selectedImageUri = uri;
		cometBitmap = null;
	}

	public void setScoreData(InventorySQLHelper scoreDataB) {
		scoreData = scoreDataB;
	}

	public void changeName(String name) {
		playerName = name;
	}

	public void setPlayerUri(Uri uri) {
		selectedPlayerUri = uri;
		cometBitmap = null;
	}

	public void setShootReverse(Boolean shot) {
		shootReverse = shot;
	}

	public void reset() {
		// reset everything
		gameOver = false;
		gamePaused = true;
		introScreenOver = false;
		enemiesPerLevel = enemiesPerLevelConstant;
		player1Score = 0;
		player1Lives = player1LivesStarting;
		player1StartingHealth = 20;
		player1Health = player1StartingHealth;
		enemiesKilled = 0;
		enemiesKilledThisLevel = 0;
		weaponLevel = 0;
		level = 1;
		player1Pos.x = mwidth / 2;
		player1Pos.y = mheight - player1Size;
		enemyDamage = emenyStartDamage;
		numberOfEnemiesAllowed = initialNumberOfEnemies;
		numberOfBulletsAllowed = maxBulletSize;

		// clear lists
		enemyList = new ArrayList();
		bulletList = new ArrayList();
		bloodList = new ArrayList();

		cometBitmap = null;
		cometBitmapLarge = null;
		cometBitmapSmall = null;
		player1Bitmap = null;
	}

	float fdistance(float x1, float y1, float x2, float y2) {
		return (float) Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
	}

	public Panel(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		//
		surfaceCreated = false;

		reset();

		getHolder().addCallback(this);
		setFocusable(true);
	}

	public void createThread(SurfaceHolder holder) {
		canvasthread = new CanvasThread(getHolder(), this, mContext,
				new Handler());
		canvasthread.setRunning(true);
		canvasthread.start();
	}

	public void terminateThread() {
		canvasthread.setRunning(false);
		try {
			canvasthread.join();
		} catch (InterruptedException e) {

		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		reset();

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		//
		if (surfaceCreated == false) {
			createThread(holder);
			// Bitmap kangoo = BitmapFactory.decodeResource(getResources(),
			// R.drawable.kangoo);
			surfaceCreated = true;
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		surfaceCreated = false;

	}

	public void addBullet() {

		if (bulletList.size() > numberOfBulletsAllowed) {
			return;
		}

		// find player position and direction orientation
		int x = player1Wants.x;
		int y = player1Wants.y;

		int deltax = player1Wants.x - player1Pos.x;
		int deltay = player1Wants.y - player1Pos.y;

		if (deltax == 0) {
			deltax = 1;
		}
		if (deltay == 0) {
			deltay = 1;
		}

		Boolean bothInBounds = true;
		Boolean xOutOfBounds = false;
		Boolean yOutOfBounds = false;
		while (bothInBounds == true) {
			if (shootReverse == true) {
				x -= deltax;
				y -= deltay;
			} else {
				x += deltax;
				y += deltay;

			}
			xOutOfBounds = testXBounds(x);
			yOutOfBounds = testYBounds(y);
			if ((xOutOfBounds == true) && (yOutOfBounds == true)) {
				bothInBounds = false;
			}
		}

		// add a bullet to bullet list with movement direction vector
		Bullet ourBullet = new Bullet(player1Pos.x, player1Pos.y, x, y);
		Bullet bulletTwo = new Bullet(ourBullet);
		Bullet bulletThree = new Bullet(ourBullet);
		Bullet bulletFour = new Bullet(ourBullet);
		Bullet bulletFive = new Bullet(ourBullet);

		switch (weaponLevel) {
		case 1:
			ourBullet.x -= 4;
			bulletList.add(ourBullet);
			bulletTwo.x += 8;
			bulletList.add(bulletTwo);
			break;
		case 2:
			bulletList.add(ourBullet);
			bulletTwo.x -= 5;
			bulletList.add(bulletTwo);
			bulletThree.x += 5;
			bulletList.add(bulletThree);
			break;
		case 3:
			bulletList.add(ourBullet);
			// if our original bullet destination is offscreen in one direction
			// our other two bullets need to have their x dest changed major neg
			// and major pos
			bulletTwo.xdest -= (mwidth / 2);
			bulletList.add(bulletTwo);
			bulletThree.xdest += (mwidth / 2);
			bulletList.add(bulletThree);
			break;
		case 4:
			ourBullet.x -= 4;
			bulletList.add(ourBullet);
			bulletTwo.x += 8;
			bulletList.add(bulletTwo);
			break;
		default:
			bulletList.add(ourBullet);
			break;
		}

	}

	Boolean testXBounds(int x) {
		if (x < 0) {
			return true;
		}
		if (x > mwidth) {
			return true;
		}
		return false;
	}

	Boolean testYBounds(int y) {
		if (y < 0) {
			return true;
		}
		if (y > mheight) {
			return true;
		}
		return false;
	}

	public void movePlayer1Tick() {
		if (introScreenOver == false) {
			player1Pos.x = mwidth / 2;
			player1Pos.y = mheight - player1Size;
			introScreenOver = true;
		}

		int x = player1Wants.x;
		int y = player1Wants.y;

		int leftx = mwidth / 4;
		int rightx = mwidth - (mwidth / 4);
		int highy = mheight - (mheight / 6);

		// make sure x good left
		if (x < leftx) {
			x = leftx;
		}
		// make sure x good right
		if (x > rightx) {
			x = rightx;
		}
		// make sure y good top
		if (y < highy) {
			y = highy;
		}

		if (x > (player1Pos.x + (player1Size / 2))) {
			if ((x - player1Pos.x) > player1Step) {
				player1Pos.x += player1Step;
			}
		} else if (x < (player1Pos.x)) {
			if (player1Pos.x > 0) {
				if ((player1Pos.x - x) >= player1Step) {
					player1Pos.x -= player1Step;
				} else {
					player1Pos.x -= (player1Pos.x - x);
				}
			}
		}

		// now for a mirror Y tick
		if (y > (player1Pos.y + (player1Size / 2))) {
			if ((y - player1Pos.y) > player1Step) {
				player1Pos.y += player1Step;
			}
		} else if (y < (player1Pos.y)) {
			if (player1Pos.y > 0) {
				if ((player1Pos.y - y) >= player1Step) {
					player1Pos.y -= player1Step;
				} else {
					player1Pos.y -= (player1Pos.y - y);
				}
			}
		}

	}

	public void moveBulletsTick() {
		int numBullets = bulletList.size();

		if (numBullets < 1) {
			return;
		}
		// if we reverse iterate over list we can remove items without worry
		// because we'll be removing from end down
		for (int i = numBullets - 1; i > 0; i--) {
			// increment and re-set each bullet after movement
			if (bulletList.size() > i) {

				Bullet ourBullet;
				try {
					ourBullet = incrementBulletOnLine((Bullet) bulletList
							.get(i));
				} catch (Exception e) {
					//
					return;
				}
				try {
					bulletList.set(i, ourBullet);
				} catch (Exception e) {
					//
					return;
				}
				// if we've already done a canvas draw
				if (mwidth > 0) {
					if ((ourBullet.x >= mwidth) || (ourBullet.x < 0)
							|| (ourBullet.y >= mheight) || (ourBullet.y < 0)) {
						try {
							bulletList.remove(i);
						} catch (Exception e) {
							//
							return;
						}
					}
					if ((ourBullet.x == ourBullet.xdest)
							&& (ourBullet.y == ourBullet.ydest)) {
						try {
							bulletList.remove(i);
						} catch (Exception e) {
							//
							return;
						}
					}
				}
			}

		}

	}

	public void moveEnemiesTick() {
		for (int i = 0; i < enemyList.size(); i++) {
			Enemy enemy;
			try {
				enemy = enemyList.get(i);
			} catch (Exception e) {
				//
				return;
			}

			// we won't move the ball the first tick so we have the correct
			// screen
			// width and hieght
			if ((mwidth == mheight) && (mwidth == 0)) {
				return;
			}

			try {
				enemyList.set(i, incrementEnemyOnLine(enemy));
			} catch (Exception e) {
				// 
				return;
			}

		}
	}

	public void testForCollisionAndProcess() {

		// loop through all comets
		Rect playerRect = new Rect();
		playerRect.left = player1Pos.x - player1Size / 2;
		playerRect.right = player1Pos.x + player1Size / 2;
		playerRect.top = player1Pos.y + player1Size / 2;
		playerRect.bottom = player1Pos.y - player1Size / 2;

		for (int i = enemyList.size() - 1; i >= 0; i--) {

			Enemy enemy;
			try {
				enemy = enemyList.get(i);
			} catch (Exception e) {
				//
				return;
			}

			int localEnemySize = cometSize;
			if (enemy.size == 0) {
				localEnemySize = cometSize / 2;
			} else if (enemy.size == 2) {
				localEnemySize = cometSize * 2;
			}

			Rect enemyRect = new Rect();
			enemyRect.left = enemy.x - localEnemySize / 2;
			enemyRect.right = enemy.x + localEnemySize / 2;
			enemyRect.top = enemy.y + localEnemySize / 2;
			enemyRect.bottom = enemy.y - localEnemySize / 2;

			// test if player hit a comet
			boolean collision = doTheyOverlap(enemyRect, playerRect);
			// CollisionTest(playerRect,cometRect);
			if (collision != false) {
				decrementHealth();
				player1Score -= 100;
				try {
					enemyList.remove(i);
				} catch (Exception e) {
					// 
					return;
				}
				return;
			}

			Boolean changedComet = false;
			for (int j = bulletList.size() - 1; j > 0; j--) {
				if (bulletList.size() > i) {
					if (i >= 0) {
						Bullet localBullet;
						try {
							localBullet = bulletList.get(j);
						} catch (Exception e) {
							//
							return;
						}

						if ((localBullet.x <= enemyRect.right)
								&& (localBullet.x >= enemyRect.left)) {

							if ((localBullet.y >= enemyRect.bottom)
									&& (localBullet.y <= enemyRect.top)) {
								// we hit this comet with a bullet
								enemy.healthPoints--;
								player1Score += 50;
								changedComet = true;
								if (weaponLevel > 3) {
									enemy.healthPoints -= boomStickHurtValue;
									boomstick(enemyRect, i, 0);
								}
								try {
									bulletList.remove(j);
								} catch (Exception e) {
									// 
									return;
								}
							}

						}
					}
				}
			}
			if (changedComet) {
				try {
					enemyList.set(i, enemy);
				} catch (Exception e) {
					// 
					return;
				}
			}

		}
	}

	public void boomstick(Rect playerRect, Integer offset, Integer depth) {

		if (depth == maxDepthValue) {
			return;
		}
		for (int j = 0; j < enemyList.size(); j++) {
			if (j != offset) {
				Enemy enemy;
				try {
					enemy = enemyList.get(j);
				} catch (Exception e) {
					//
					return;
				}

				if (enemy.healthPoints > 0) {
					int localEnemySize = cometSize;
					if (enemy.size == 0) {
						localEnemySize = cometSize / 2;
					} else if (enemy.size == 2) {
						localEnemySize = cometSize * 2;
					}

					Rect enemyRect = new Rect();
					enemyRect.left = enemy.x - localEnemySize / 2;
					enemyRect.right = enemy.x + localEnemySize / 2;
					enemyRect.top = enemy.y + localEnemySize / 2;
					enemyRect.bottom = enemy.y - localEnemySize / 2;

					// test if player hit a comet
					boolean collision = doTheyOverlap(enemyRect, playerRect);
					if (collision) {
						enemy.healthPoints -= boomStickHurtValue;
						boomstick(enemyRect, j, depth + 1);
						return;
					}
				}
			}
		}
	}

	public boolean betweenOrOn(int a, int b, int c) {
		if (a >= b) {
			if (a <= c) {
				return true;
			}
		}
		return false;
	}

	public boolean doTheyOverlap(Rect one, Rect two) {

		// left side of one is in two
		if (betweenOrOn(one.left, two.left, two.right)) {
			// top side of one is in two
			if (betweenOrOn(one.top, two.bottom, two.top)) {
				return true;
			}

			// bottom side of one is in two
			if (betweenOrOn(one.bottom, two.bottom, two.top)) {
				return true;
			}

			// one is bigger and contains two
			if (betweenOrOn(two.bottom, one.bottom, one.top)
					&& betweenOrOn(two.top, one.bottom, one.top)) {
				return true;
			}

		}
		// right side of one is in two
		// left side of one is in two
		if (betweenOrOn(one.right, two.left, two.right)) {
			// top side of one is in two
			if (betweenOrOn(one.top, two.bottom, two.top)) {
				return true;
			}

			// bottom side of one is in two
			if (betweenOrOn(one.bottom, two.bottom, two.top)) {
				return true;
			}
			// one is bigger and contains two
			if (betweenOrOn(two.bottom, one.bottom, one.top)
					&& betweenOrOn(two.top, one.bottom, one.top)) {
				return true;
			}
		}

		// one is bigger and contains two
		if (betweenOrOn(two.left, one.left, one.right)
				&& betweenOrOn(two.right, one.left, one.right)) {
			// top side of one is in two
			if (betweenOrOn(one.top, two.bottom, two.top)) {
				return true;
			}

			// bottom side of one is in two
			if (betweenOrOn(one.bottom, two.bottom, two.top)) {
				return true;
			}
			// one is bigger and contains two
			if (betweenOrOn(two.bottom, one.bottom, one.top)
					&& betweenOrOn(two.top, one.bottom, one.top)) {
				return true;
			}
		}

		return false;
	}

	public void updateBloodTicks() {
		for (int i = bloodList.size() - 1; i >= 0; i--) {
			bloodPoint bp;
			try {
				bp = bloodList.get(i);
			} catch (Exception e) {
				//
				return;
			}

			bp.age++;
			if (bp.age > bloodTTL) {
				try {
					bloodList.remove(i);
				} catch (Exception e) {
					//
					return;
				}
			}
		}
	}

	public void saveHighScore() {
		SQLiteDatabase db = scoreData.getWritableDatabase();
		ContentValues values = new ContentValues();
		String scoreString = level + " " +playerName;
		values.put(InventorySQLHelper.NAMES, scoreString);
		values.put(InventorySQLHelper.SCORES, player1Score);
		long latestRowId = db.insert(InventorySQLHelper.TABLE, null, values);
		db.close();
	}

	public void decrementHealth() {

		player1Health -= enemyDamage;

		if (player1Health < 1) {
			gamePaused = true;
			player1Lives--;
			player1Health = player1StartingHealth;
			if (player1Lives < 1) {
				gameOver = true;
				gamePaused = false;
				saveHighScore();

				// let's make some blood!!!!
				bloodPoint bp;
				for (int i = 0; i < numberOfBloodSpots; i++) {
					bp = new bloodPoint();
					bloodList.add(bp);
				}

			}

			// move the player to the middle
			player1Pos.x = mwidth / 2;
			player1Pos.y = mheight - player1Size;

			// move all comets to the edge
			// loop through all comets
			for (int i = 0; i < enemyList.size(); i++) {

				Enemy enemy;
				try {
					enemy = enemyList.get(i);
				} catch (Exception e) {
					//
					return;
				}
				if (enemy.left == true) {
					if (enemy.down == true) {
						enemy.x = 0;
					} else {
						enemy.x = mwidth;
					}
				} else {
					if (enemy.down == true) {
						enemy.y = 0;
					} else {
						enemy.y = 0;
					}
				}

				try {
					enemyList.set(i, enemy);
				} catch (Exception e) {
					//
					return;
				}

			}

		}

	}

	public void updateGameState() {

		if (gameOver == true) {
			return;
		}

		if (gamePaused == true) {
			return;
		}

		// make sure there's a graphics init round
		if (mwidth == 0) {
			return;
		}

		// update the score a point for being alive
		player1Score++;

		// make sure there are enough comets onscreen
		int cometDiff = numberOfEnemiesAllowed - enemyList.size();
		if (cometDiff > 0) {
			for (int i = 0; i < cometDiff; i++) {
				generateEnemy();
			}
		}

		// move player 1 a tick
		movePlayer1Tick();

		// move all asteroids a tick
		moveEnemiesTick();

		// move bulletts a tick
		for (int i = 0; i < bulletStep; i++) {
			moveBulletsTick();
		}

		// test for bullet or player collision
		testForCollisionAndProcess();

		updateBloodTicks();

	}

	public void generateEnemy() {

		int randomwElement = myrandom.nextInt(mwidth);
		int randomhElement = myrandom.nextInt(mheight - (mheight / 3));
		int whichElement = myrandom.nextInt(3);
		int x = 0;
		int y = 0;

		switch (whichElement) {
		case 0:
			// left edge
			y = randomhElement;
			break;
		case 1:
			// right edge
			y = randomhElement;
			x = mwidth;
			break;
		case 2:
			// top edge
			x = randomwElement;
			break;
		case 3:
			// bottom edge not allowd
			x = randomwElement;
			y = 0;
			break;
		default:
			break;
		}

		//
		Enemy myComet = new Enemy(x, y);
		enemyList.add(myComet);

	}

	@Override
	public void onDraw(Canvas canvas) {

		mwidth = canvas.getWidth();
		mheight = canvas.getHeight();

		Paint paint = new Paint();

		// our player sizes should be a function both of difficulty and of
		// screen size
		int visualDivisor = mwidth;
		if (mheight < mwidth) {
			visualDivisor = mheight;
		}
		player1Size = visualDivisor / 8;
		cometSize = visualDivisor / 12;
		int cometSizeSmall = cometSize / 2;
		int cometSizeLarge = cometSize * 2;

		// draw player 1
		if (introScreenOver == true) {
			// Draw all the blood from the last round
			paint.setColor(enemyBloodColor);
			for (int i = bloodList.size() - 1; i >= 0; i--) {
				bloodPoint bp;
				try {
					bp = bloodList.get(i);
				} catch (Exception e) {
					//
					return;
				}
				canvas.drawPoint(bp.x, bp.y, paint);
			}

			if (player1Bitmap == null) {
				// cometSize = mwidth / 5;
				// if we can't load somebody else's bitmap
				if (selectedPlayerUri == null) {
					Bitmap _scratch = BitmapFactory.decodeResource(
							getResources(), R.drawable.trollface);

					if (_scratch == null) {
						Toast.makeText(getContext(), "WTF", Toast.LENGTH_SHORT)
								.show();
					}

					// now scale the bitmap using the scale value
					player1Bitmap = Bitmap.createScaledBitmap(_scratch,
							player1Size, player1Size, false);
					player1IconBitmap = Bitmap.createScaledBitmap(_scratch,
							player1IconSize, player1IconSize, false);
				} else {
					// THIS IS WHERE YOU LOAD FILE URIS AT
					InputStream photoStream = null;

					Context context = getContext();
					try {
						photoStream = context.getContentResolver()
								.openInputStream(selectedPlayerUri);
					} catch (FileNotFoundException e) {
						//
						e.printStackTrace();
					}
					int scaleSize = decodeFile(photoStream, player1Size,
							player1Size);

					try {
						photoStream = context.getContentResolver()
								.openInputStream(selectedPlayerUri);
					} catch (FileNotFoundException e) {
						//
						e.printStackTrace();
					}
					BitmapFactory.Options o = new BitmapFactory.Options();
					o.inSampleSize = scaleSize;

					Bitmap photoBitmap = BitmapFactory.decodeStream(
							photoStream, null, o);
					player1Bitmap = Bitmap.createScaledBitmap(photoBitmap,
							cometSize, cometSize, true);
					player1IconBitmap = Bitmap.createScaledBitmap(photoBitmap,
							player1IconSize, player1IconSize, false);
					photoBitmap.recycle();

				}

			}
			canvas.drawBitmap(player1Bitmap, player1Pos.x - (player1Size / 2),
					player1Pos.y - (player1Size / 2), paint);

		}// introscreenover = true
			// draw bullets
		paint.setColor(bulletColor);
		int numBullets = bulletList.size();
		for (int i = numBullets - 1; i > 0; i--) {
			if (bulletList.size() > i) {
				if (i >= 0) {
					Bullet tempBullet;
					try {
						tempBullet = bulletList.get(i);
					} catch (Exception e) {
						//
						return;
					}

					canvas.drawCircle(tempBullet.x, tempBullet.y, 1, paint);
				}
			}
		}

		// draw comets
		if (cometBitmap == null) {
			// cometSize = mwidth / 5;
			// if we can't load somebody else's bitmap
			if (selectedImageUri == null) {
				Bitmap _scratch = BitmapFactory.decodeResource(getResources(),
						R.drawable.megusta);

				if (_scratch == null) {
					Toast.makeText(getContext(), "WTF", Toast.LENGTH_SHORT)
							.show();
				}

				// now scale the bitmap using the scale value
				cometBitmap = Bitmap.createScaledBitmap(_scratch, cometSize,
						cometSize, false);
				cometBitmapLarge = Bitmap.createScaledBitmap(_scratch,
						cometSizeLarge, cometSizeLarge, false);
				cometBitmapSmall = Bitmap.createScaledBitmap(_scratch,
						cometSizeSmall, cometSizeSmall, false);
			} else {
				//
				// THIS IS WHERE YOU LOAD FILE URIS AT
				InputStream photoStream = null;

				Context context = getContext();
				try {
					photoStream = context.getContentResolver().openInputStream(
							selectedImageUri);
				} catch (FileNotFoundException e) {
					//
					e.printStackTrace();
				}
				int scaleSize = decodeFile(photoStream, cometSize, cometSize);

				try {
					photoStream = context.getContentResolver().openInputStream(
							selectedImageUri);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				BitmapFactory.Options o = new BitmapFactory.Options();
				o.inSampleSize = scaleSize;

				Bitmap photoBitmap = BitmapFactory.decodeStream(photoStream,
						null, o);
				cometBitmap = Bitmap.createScaledBitmap(photoBitmap, cometSize,
						cometSize, true);
				cometBitmapLarge = Bitmap.createScaledBitmap(photoBitmap,
						cometSizeLarge, cometSizeLarge, false);
				cometBitmapSmall = Bitmap.createScaledBitmap(photoBitmap,
						cometSizeSmall, cometSizeSmall, false);
				photoBitmap.recycle();

			}

		}

		// draw the comet bitmaps all over
		// for each comet
		for (int i = enemyList.size(); i > 0; i--) {
			int enemyListSize = enemyList.size();
			if (enemyListSize > i) {

				Enemy myComet;
				try {
					myComet = enemyList.get(i);
				} catch (Exception e) {
					//
					return;
				}

				switch (myComet.size) {
				case 0:
					if (myComet.healthPoints < 1) {
						drawExplosion(canvas, myComet.x - (cometSizeSmall / 2),
								myComet.x + (cometSizeSmall / 2), myComet.y
										+ (cometSizeSmall / 2), myComet.y
										- (cometSizeSmall / 2));
						try {
							enemyList.remove(i);
						} catch (Exception e) {
							//
							return;
						}
						incrementEnemiesKilled(
								myComet.x - (cometSizeSmall / 2), myComet.y
										- (cometSizeSmall / 2), cometSizeSmall);
					} else {
						canvas.drawBitmap(cometBitmapSmall, myComet.x
								- (cometSizeSmall / 2), myComet.y
								- (cometSizeSmall / 2), paint);
						drawHealth(canvas, myComet.x - (cometSizeSmall / 2),
								myComet.x + (cometSizeSmall / 2), myComet.y
										+ (cometSizeSmall / 2), myComet.y
										- (cometSizeSmall / 2), 100);

					}
					break;
				case 1:
					if (myComet.healthPoints < 1) {
						drawExplosion(canvas, myComet.x - (cometSize / 2),
								myComet.x + (cometSize / 2), myComet.y
										+ (cometSize / 2), myComet.y
										- (cometSize / 2));
						try {
							enemyList.remove(i);
						} catch (Exception e) {
							//
							return;
						}
						incrementEnemiesKilled(myComet.x - (cometSize / 2),
								myComet.y - (cometSize / 2), cometSize);
						enemyList.add(new Enemy(myComet.x, myComet.y, 0, 1,
								false, false));

					} else if (myComet.healthPoints < 2) {
						canvas.drawBitmap(cometBitmap, myComet.x
								- (cometSize / 2), myComet.y - (cometSize / 2),
								paint);
						drawCracks(canvas, myComet.x - (cometSize / 2),
								myComet.x + (cometSize / 2), myComet.y
										+ (cometSize / 2), myComet.y
										- (cometSize / 2));
						drawHealth(canvas, myComet.x - (cometSize / 2),
								myComet.x + (cometSize / 2), myComet.y
										+ (cometSize / 2), myComet.y
										- (cometSize / 2), 50);
					} else {
						canvas.drawBitmap(cometBitmap, myComet.x
								- (cometSize / 2), myComet.y - (cometSize / 2),
								paint);
						drawHealth(canvas, myComet.x - (cometSize / 2),
								myComet.x + (cometSize / 2), myComet.y
										+ (cometSize / 2), myComet.y
										- (cometSize / 2), 100);
					}
					break;
				case 2:
					if (myComet.healthPoints < 1) {
						drawExplosion(canvas, myComet.x - (cometSizeLarge / 2),
								myComet.x + (cometSizeLarge / 2), myComet.y
										+ (cometSizeLarge / 2), myComet.y
										- (cometSizeLarge / 2));
						try {
							enemyList.remove(i);
						} catch (Exception e) {
							//
							return;
						}
						incrementEnemiesKilled(
								myComet.x - (cometSizeLarge / 2), myComet.y
										- (cometSizeLarge / 2), cometSizeLarge);
						enemyList.add(new Enemy(myComet.x, myComet.y, 1, 2,
								false, false));
					} else if (myComet.healthPoints < 4) {
						canvas.drawBitmap(cometBitmapLarge, myComet.x
								- (cometSizeLarge / 2), myComet.y
								- (cometSizeLarge / 2), paint);
						drawCracks(canvas, myComet.x - (cometSizeLarge / 2),
								myComet.x + (cometSizeLarge / 2), myComet.y
										+ (cometSizeLarge / 2), myComet.y
										- (cometSizeLarge / 2));
						drawHealth(canvas, myComet.x - (cometSizeLarge / 2),
								myComet.x + (cometSizeLarge / 2), myComet.y
										+ (cometSizeLarge / 2), myComet.y
										- (cometSizeLarge / 2),
								(25 * myComet.healthPoints));
					} else {

						canvas.drawBitmap(cometBitmapLarge, myComet.x
								- (cometSizeLarge / 2), myComet.y
								- (cometSizeLarge / 2), paint);
						drawHealth(canvas, myComet.x - (cometSizeLarge / 2),
								myComet.x + (cometSizeLarge / 2), myComet.y
										+ (cometSizeLarge / 2), myComet.y
										- (cometSizeLarge / 2), 100);
					}

					break;
				default:
					Toast.makeText(getContext(), "Error in Enemy Rendering",
							Toast.LENGTH_SHORT).show();
					break;
				}
			}
		}
		// - draw comet in its position
		// - draw comet with its level of asplosion
		// - after drawing a comet that's fully asploded
		// - - remove it from the list
		// - - - generate a new comet
		// generateComet();
		if (introScreenOver == true) {
			// draw score 1
			paint.setColor(scoreColor);
			// draw score 2
			String nameString;
			nameString = playerName;
			canvas.drawText(nameString, 0, 22, paint);
			nameString = "Score:" + String.valueOf(player1Score);
			canvas.drawText(nameString, 0, 9, paint);

			// for each live left draw a tiny life bitmap
			for (int i = 0; i < player1Lives; i++) {
				Paint painter = new Paint();
				canvas.drawBitmap(player1IconBitmap,
						((3 + player1IconSize) * i), 24, painter);
			}

			// draw the level number
			String levelString = "Level " + level;
			canvas.drawText(levelString, mwidth - 50, 9, paint);
			// draw the number of kills
			levelString = enemiesKilled + " Kills";
			canvas.drawText(levelString, mwidth - 50, 22, paint);
			// draw the weapon
			levelString = getWeaponString();
			canvas.drawText(levelString, 0, 62, paint);

			// draw a health bar
			paint.setColor(healthBarColor);
			int healthBarRight = player1Health;
			canvas.drawRect(0, 40, healthBarRight, 50, paint);

		}
		// draw game over if game over
		if (gameOver == true) {

			paint.setTextSize(20);
			canvas.drawText("Game Over", (mwidth / 2) - 50, mheight / 4, paint);

			paint.setColor(enemyBloodColor);
			bloodPoint bp;
			for (int i = 0; i < bloodList.size(); i++) {

				try {
					bp = bloodList.get(i);
				} catch (Exception e) {
					//
					return;
				}
				canvas.drawPoint(bp.x, bp.y, paint);
			}

		}

		if (gamePaused == true) {
			paint.setTextSize(25);
			paint.setColor(enemyBloodColor);
			canvas.drawText("Touch To Continue", (mwidth / 2) - 110,
					mheight - (mheight / 5), paint);
		}

	}

	public void incrementEnemiesKilled(int left, int bottom, int size) {

		int xt;
		int yt;
		for (int i = 0; i < (size * 3); i++) {
			xt = left + myrandom.nextInt(size);
			yt = bottom + myrandom.nextInt(size);
			bloodPoint bp = new bloodPoint(xt, yt, 0);
			bloodList.add(bp);
		}

		enemiesKilledThisLevel++;
		enemiesKilled++;
		if (enemiesKilledThisLevel >= enemiesPerLevel) {
			level++;
			levelUpCharacter();
			enemiesKilledThisLevel = 0;
			numberOfEnemiesAllowed++;
			numberOfBulletsAllowed += 5;
			enemiesPerLevel += enemiesPerLevelConstant;
		}
	}

	public void levelUpCharacter() {
		player1StartingHealth += level * 2;
		player1Health += level;

		if (level == 3) {
			weaponLevel++;
		}
		if (level == 6) {
			weaponLevel++;
		}
		if (level == 12) {
			weaponLevel++;
		}
		if (level == 20) {
			weaponLevel++;
		}

	}

	public String getWeaponString() {
		switch (weaponLevel) {
		case 1:
			return "Double Pistol";
		case 2:
			return "Triple Pistol";
		case 3:
			return "Shotgun";
		case 4:
			return "BOOMSTICK";
		default:
			return "Pistol";

		}
	}

	private void drawHealth(Canvas canvas, int left, int right, int top,
			int bottom, int percentage) {
		Paint paint = new Paint();
		paint.setColor(enemyHealthColor);

		int oldWidth = right - left;
		double newWidth = (.01) * percentage * oldWidth;
		int newR = (int) (left + newWidth);
		int newBottom = top - (top - bottom) / 12;

		canvas.drawRect(left, top - (top - bottom) / 6, newR, newBottom, paint);

	}

	private void drawCracks(Canvas canvas, int left, int right, int top,
			int bottom) {
		Paint paint = new Paint();
		paint.setColor(Color.WHITE);
		paint.setAntiAlias(true);
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(1);
		int xa, xb, ya, yb;
		int width = right - left;
		int height = top - bottom;
		for (int i = 0; i < numCracks; i++) {
			xa = myrandom.nextInt(width) + left;
			xb = myrandom.nextInt(width) + left;
			ya = myrandom.nextInt(height) + bottom;
			yb = myrandom.nextInt(height) + bottom;
			canvas.drawLine(xa, ya, xb, yb, paint);
		}
	}

	private void drawExplosion(Canvas canvas, int left, int right, int top,
			int bottom) {
		int x = (left + right) / 2;
		int y = (top + bottom) / 2;
		int radius = (right - left) / 2;

		Paint paint = new Paint();
		paint.setColor(Color.WHITE);
		paint.setAntiAlias(true);
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(1);
		canvas.drawCircle(x, y, radius, paint);

		canvas.drawLine(x - radius / 2, y + radius / 2, x + radius / 2, y
				+ radius / 2, paint);

		int eyestop = y - radius / 4;
		int eyesbottom = y - radius / 2;
		int lefteyeleft = x - radius / 2;
		int lefteyeright = x - radius / 4;
		int righteyeleft = x + radius / 4;
		int righteyeright = x + radius / 2;

		// left eye x
		canvas.drawLine(lefteyeleft, eyestop, lefteyeright, eyesbottom, paint);
		canvas.drawLine(lefteyeright, eyestop, lefteyeleft, eyesbottom, paint);

		// right eye x
		canvas.drawLine(righteyeleft, eyestop, righteyeright, eyesbottom, paint);
		canvas.drawLine(righteyeright, eyestop, righteyeleft, eyesbottom, paint);

	}

	// decodes image and scales it to reduce memory consumption
	private int decodeFile(InputStream photostream, int h, int w) {
		// Decode image size
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(photostream, null, o);

		// Find the correct scale value. It should be the power of 2.
		int width_tmp = o.outWidth, height_tmp = o.outHeight;
		int scale = 1;
		while (true) {
			if (width_tmp / 2 < w || height_tmp / 2 < h)
				break;
			width_tmp /= 2;
			height_tmp /= 2;
			scale *= 2;
		}

		return scale;
	}

	public Enemy incrementEnemyOnLine(Enemy enemy) {
		int x0 = enemy.x;
		int y0 = enemy.y;
		int x1 = player1Pos.x;
		int y1 = player1Pos.y;
		int dx = Math.abs(x1 - x0);
		int dy = Math.abs(y1 - y0);
		int sx = -1;
		if (x0 < x1) {
			sx = 1;
		}
		int sy = -1;
		if (y0 < y1) {
			sy = 1;
		}

		int err = dx - dy;
		Enemy returnEnemy = new Enemy(enemy);

		Boolean running = true;
		while (running) {
			// iterate till finished
			if ((x0 == x1) && (y0 == y1)) {
				running = false;
			} else {
				int e2 = 2 * err;
				if (e2 > -dy) {
					err = err - dy;
					x0 = x0 + sx;
					running = false;
				}
				if (e2 < dx) {
					err = err + dx;
					y0 = y0 + sy;
					running = false;
				}
			}

		}
		returnEnemy.x = x0;
		returnEnemy.y = y0;
		return returnEnemy;
	}

	public Bullet incrementBulletOnLine(Bullet mybullet) {
		int x0 = mybullet.x;
		int y0 = mybullet.y;
		int x1 = mybullet.xdest;
		int y1 = mybullet.ydest;
		int dx = Math.abs(x1 - x0);
		int dy = Math.abs(y1 - y0);
		int sx = -1;
		if (x0 < x1) {
			sx = 1;
		}
		int sy = -1;
		if (y0 < y1) {
			sy = 1;
		}
		;
		int err = dx - dy;
		Bullet returnBullet = new Bullet(mybullet);

		Boolean running = true;
		while (running) {
			// iterate till finished
			if ((x0 == x1) && (y0 == y1)) {
				running = false;
			} else {
				int e2 = 2 * err;
				if (e2 > -dy) {
					err = err - dy;
					x0 = x0 + sx;
					running = false;
				}
				if (e2 < dx) {
					err = err + dx;
					y0 = y0 + sy;
					running = false;
				}
			}

		}
		returnBullet.x = x0;
		returnBullet.y = y0;
		return returnBullet;

	}// end line draw

} // end class