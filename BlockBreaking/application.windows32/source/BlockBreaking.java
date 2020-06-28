import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import processing.video.*; 
import gab.opencv.*; 
import java.awt.Rectangle; 
import ddf.minim.*; 
import ddf.minim.ugens.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class BlockBreaking extends PApplet {

//トラッキング用クラス
Tracking tracking;

//フィールドの設定
SpaceData space;


//カメラを使用するか
//使用しない場合マウスで代用
boolean cameraEnable = true;

//カメラ画像
PImage cameraCapture;

//ボール用のリスト
ArrayList<Ball> ball;
//弾用のリスト
ArrayList<Bullet> bullet;


//ブロックの最大数
int max_block_count = 450;
//ブロック用の配列
Block[] block = new Block[max_block_count]; 
//ブロックが存在しているかの配列
boolean[] blockEnableList = new boolean[max_block_count];
//次に出現させるブロック番号
int nextBlockNum = 0;

//ラケットの中心座標
int racket_center;
//ラケットの幅
int racket_width = 100;

//ポインターのY座標
//0をラケットのY座標とする
int pointer_y;


//ブロックの画像データ
PImage[] blockImg = new PImage[10];

//ポインターの画像
PImage pointer;

//弾の画像
PImage bulletImg;

//矢印の画像
PImage arrowImg;


//フォントの設定
PFont font;

//フィールドの情報
/*
 fieldData[x][y]
一ピクセルごとにブロックの情報を入れる
入っている番号はブロック番号と同じ
何もないところには-1が入る
*/
int[][] fieldData = new int[680][500];

//次にデータフィールドに入る値を入れておく変数
int[] nextFieldData = new int[500];


//画面サイズは固定
//ボールサイズ15
//ブロックサイズ50x20

//ブロックの管理
//ブロック用の配列を600個作る
//0から順に出現させ600まで行ったら0に戻す
//このとき画面上に現れるブロックの最大数より多い配列を作る


//ブロック移動の開始フラグ
boolean blockMoveEnable = false;


//スコア
//計算式
//一ブロック当たり　100 + ((level - 1) * 10)
int score = 0;


//消費カロリー
float kcal = 0;

//本日の消費カロリー
//これは毎ゲームごとにリセットせず、日付が変わったタイミングでリセット
//ゲーム終了のたびに書き込み
float today_kcal = 0;

//スクワット　5METs
//反復横跳び　8METsとする
//計算式
// kcal = METs * 時間(h) * 体重(60kgとする)

//スクワットは、膝を曲げている時間
// kcal += 5 * 60 * (膝を曲げていた時間(s) / 3600)
// kcal += 膝を曲げていた時間(s) / 12

//反復横跳びは、端から端までを400pxとして、400(px/s)で8METsになる計算
//6フレームごとに計算
// kcal += 8 * 60 * (0.1 / 3600) * (6フレーム内での移動量 / 400)
// kcal += 6フレーム内での移動量 / 30000

//6フレームカウント
int six_frame_count = 0;

//6フレーム前のラケット座標
int pre_racket_center = 0;


//今のモード
//0: メニュー
//1: ゲーム中
//2: ゲームオーバー画面
//これ以下はアニメーション用の画面
//3: メニューからゲームへの遷移
//4: ゲームオーバーになった瞬間の遷移
//5: ゲームオーバー画面からメニューへの遷移　このタイミングでスコアの初期化
//画面遷移は0→3→1→4→2→5→0
int mode = 0;
 
public void setup() {

    //フィールドの設定
    space = new SpaceData(500,680,290,10);

    //サイズの設定
    

    //カメラを使うなら
    if(cameraEnable){
        tracking = new Tracking(this, 350, 10, 64, 255, 64, 255);
    }
    
    //ボールの作成
    ball = new ArrayList<Ball>();

    //弾の作成
    bullet = new ArrayList<Bullet>();

    //外部txt読み込み
    patternLoad();

    //消費カロリーの読み込み
    kcalLoad();

    //初期化
    init();

    //オーディオ関係の初期化
    soundInit();
    

    //画像データの読み込み imageLoad.ode
    imageLoad();

    
    //フォントの設定
    font = createFont("Meiryo UI", 80);
    textFont(font, 80);
}


//ポインターの場所
//上なら0
//下なら1
int pointer01 = 0;

//ポインターアニメーション用
int pointer_anime = 0;

//スクワットをためているフレーム数
int squat_frame = 0;

//スクワット開始していいか
//初期状態でしゃがんでいた場合に判定を見送るフラグ
//必要に応じて画面遷移のたびにfalseにする
boolean squat_flag = false;

public void draw() {
    //背景を塗りつぶし
    background(128);

    if(cameraEnable){
        //トラッキングの結果を取得
        CamPosition position = tracking.LoadPosition(); 

        //入力を変換
        racket_center = constrain(position.x - 70, 0, 499);  //大きさに応じて変更
        pointer_y = position.y - 210;
    }else{
        racket_center = mouseX - space.s_x;
        
        //ポインター関係
        pointer_y = mouseY - 330 - space.s_y; 
        pointer_y /= 2;
    }

    //ラケットが両端から出ないようにするための処理
    racket_center = constrain(racket_center, racket_width / 2, space.s_width - (racket_width / 2));

    //ポインターが画面外に出ないようにするための処理
    pointer_y = constrain(pointer_y, 0, 75);

    //ポインターの場所
    if(pointer01 == 0){
        squat_flag = true;
        squat_frame = 0;
        if(pointer_y >= 50){
            pointer01 = 1;
        }
    }else{
        if(pointer_y <= 25){
            pointer01 = 0;

            //ゲーム中なら
            if(mode == 1){
                if(squat_frame >= 110){
                    //ボール追加
                    ball.add(new Ball(this, racket_center, 560));
                    ball.get(ball.size() - 1).setSpeedWithAngle(5,60);
                    ball.get(ball.size() - 1).ballStart();
                    ball.add(new Ball(this, racket_center, 560));
                    ball.get(ball.size() - 1).setSpeedWithAngle(5,120);
                    ball.get(ball.size() - 1).ballStart();
                    bullet.add(new Bullet(this, racket_center - 5, 560, 10));

                    //ボール発射SEを鳴らす
                    addBall.trigger();
                }else{
                    //玉発射
                    bullet.add(new Bullet(this, racket_center - 5, 560, (squat_frame / 12) + 1));

                    //弾発射SEを鳴らす
                    hassya.trigger();
                }

                //カロリー計算
                kcal += squat_frame / 720.0f;
                today_kcal += squat_frame / 720.0f;

            }
        }
    }

    
    //フィールドを白で塗りつぶし
    fill(255);
    noStroke();
    rect(space.s_x, space.s_y, space.s_width, space.s_height);

    //カメラオンならカメラを表示する
    if(cameraEnable){
        image(tracking.inverseCap, 290, 200, 500, 280);
        
        //カメラを半透明にする
        noStroke();
        fill(255, 255, 255, 128);   //要調整
        rect(290, 200, 500, 280);
    }


    //モードによる処理
    switch(mode){
        case 0:     //メニュー画面
            menu();
            break;

        case 1:     //ゲーム画面
            game();
            break;

        case 2:     //ゲームオーバー画面
            //gameOver();
            //上にかぶせる必要があるためここではなく最後に呼び出す
            break;

        case 3:     //ゲーム開始アニメーション
            menu2game();
            break;

        case 4:     //ゲームオーバーアニメーション
            //gameoverAnimation();
            //上にかぶせる必要があるためここではなく最後に呼び出す
            break;

        case 5:     //メニューに戻る処理
            //over2menu();
            //上にかぶせる必要があるためここではなく最後に呼び出す
            break;
            
        default :   //例外
            println("modeの値が不正です mode=" + mode);
            break;	
    }



    //ゲームオーバーゾーンの描写
    noStroke();
    fill(255, 0, 0, (30 - abs((frameCount % 60) - 30)) * 4 + 50);
    rect(space.s_x, space.s_y + space.s_height - 100, space.s_width, 10);


    //ラケットの描画
    fill(0);
    noStroke();
    rect(space.s_x + racket_center - (racket_width / 2), space.s_y + space.s_height - 100, racket_width, 10);

    //ポインターの目標座標の描写
    fill(10);
    stroke(10);
    strokeWeight(2);
    pointer_anime++;
    if(pointer_anime >= 32){
        pointer_anime = 0;
    }
    if(pointer01 == 0){     //立っているとき
        line(290, 652, 790, 652);
        strokeWeight(2);
        stroke(pointer_anime << 4);
        line(290, 652 + pointer_anime, 790, 652 + pointer_anime);
    }else{                  //膝を曲げているとき
        line(290, 627, 790, 627);
        strokeWeight(2);
        stroke(pointer_anime << 4);
        line(290, 627 - pointer_anime, 790, 627 - pointer_anime);

        //スクワットの貯め
        if(squat_flag){
            squat_frame = min(++squat_frame, 120);
            noStroke();
            fill(200);
            circle(racket_center + 290.5f, space.s_y + 580 + pointer_y + 12.5f, 40);
            fill(150);
            arc(racket_center + 290.5f, space.s_y + 580 + pointer_y + 12.5f, 40, 40, radians(-90), radians(-90 + squat_frame * 3));
        }
    }



    //ボールの描写
    for(int i = 0 ; i < ball.size() ; i++){
        ball.get(i).ballDraw();
    }

    //ブロックの描写
    tint(255, 192);  //ブロックを半透明に
    for(int i = 0 ; i < max_block_count ; i++){
        if(blockEnableList[i]){
            block[i].blockDraw();
        }
    }
    tint(255, 255);

    //弾の描写
    for(int i = 0 ; i < bullet.size() ; i++){
        bullet.get(i).draw();
    }

    //ポインターの描写
    image(pointer, racket_center - 12 + space.s_x, pointer_y + space.s_y + 580);

    //モードによる分岐の中で、一番上に表示しなければならないもの
    if(mode == 2){
        //ゲームオーバー画面
        gameOver();
    }else if(mode == 4){
        //ゲームオーバーアニメーションの表示
        gameoverAnimation();
    }

    //枠外の描写
    noStroke();
    fill(128);
    rect(290, 0, 510, 10);
    rect(290, 690, 510, 10);

    
    //スコアの表示　
    stroke(0);
    fill(0);
    textSize(50);
    text("score", 75, 50);
    textSize(40);
    String score_text = nf(constrain(score, 0, 999999999), 9);  //上限の設定とフォーマット
    text(score_text, 30, 100);

    //カロリーの表示
    textSize(40);
    text("消費カロリー", 48, 200);
    textSize(35);
    String kcal_text = nf(kcal, 4, 4);
    text(kcal_text + "kcal", 16, 250);

    textSize(35);
    text("本日の消費カロリー", 10, 350);
    textSize(35);
    kcal_text = nf(today_kcal, 4, 4);
    text(kcal_text + "kcal", 16, 400);

    //タイトル表示
    noStroke();
    fill(230);
    rect(20, 505, 250, 100, 10);
    fill(0);
    textSize(40);
    text("フィットネス", 35, 550);
    text("ブロック崩し", 75, 590);

    //モードによる分岐の中で、一番上に表示しなければならないもの
    if(mode == 5){
        //メニュー画面への遷移
        over2menu();
    }
}


//フィールドデータを1px下に移動する
//その際1行目はnextFieldDataから代入
public void fieldMove(){
    for(int i = 679 ; i > 0 ; i--){
        for(int j = 0 ; j < 500 ; j++){
            fieldData[i][j] = fieldData[i - 1][j];
        }
        
    }
    for(int i = 0 ; i < 500 ; i++){
        fieldData[0][i] = nextFieldData[i];
    }
}

//初期化処理
public void init(){

    //ブロックの存在リストの初期化
    for(int i = 0 ; i < max_block_count ; i++){
        blockEnableList[i] = false;
    }


    //fieldDataの初期化
    for(int i = 0 ; i < 680 ; i++){
        for(int j = 0 ; j < 500 ; j++){
            fieldData[i][j] = -1;
        }
    }
    for(int i = 0 ; i < 500 ; i++){
        nextFieldData[i] = -1;
    }

    //スコア、カロリーの初期化
    score = 0;
    kcal = 0;

    //インクリメントしていくやつ
    now_count = 0;

    //何px進んだか
    px_cnt = 0;

    //何パターン出現したか 3パターンごとにリセットしlevelを1上げる
    pt_cnt = 0;
}
//ボールのクラス
//コンストラクタの引数
//(親クラス(this), ボールの初期座標X,Y)

//ボールの当たり判定用定数
//使い方
//添え字は4bitの整数
//上位ビットから左上、右上、左下、右下の順にブロックに当たっていたら1 当たっていなかったら0
//返ってくる値は
// 0 : 変更なし
// 1 : X軸方向に反射
// 2 : Y軸方向に反射
// 3 : XY軸方向に反射
// 4 : 左上が当たっている
// 5 : 右上が当たっている
// 6 : 左下が当たっている
// 7 : 右下が当たっている
final int collisionData[] = {0, 7, 6, 2, 5, 1, 3, 3, 4, 3, 1, 3, 2, 3, 3, 0};

//使い方
//初期化
//Ball()    クラスの作成
//setSpeedWithAngle()   ボールの速度設定
//ballStart() ボールが動くように設定

//毎フレーム
//ballMove()  ボールを動かす
//ballDraw()  ボールを描写する
class Ball{
    //親クラス
    PApplet parent;

    //ボールの座標
    //floatにしているのは、speed_x,speed_yが1未満だと切り捨てられてしまい動けなくなるから
    float x,y;

    //ボールの現在のスピード
    //基本的にこっちは直接いじらない
    //speedから計算して算出する
    float speed_x, speed_y;

    //ボールの速さ
    //これをいじってボールの速さを変える
    float speed;

    //今のボールが進んでいる角度
    //度数法
    float now_angle;

    //ボールのサイズ
    int b_size;

    //移動を許可するか
    boolean moveEnable;

    //ボールの色
    int ballColor;

    Ball(PApplet _parent, int _x, int _y){
        //初期値設定
        parent = _parent;
        x = _x;
        y = _y;

        speed_x = 0;
        speed_y = 0;
        speed = 0;
        now_angle = 0;

        moveEnable = false;
        b_size = 15;
        //ボールの色を黒にする
        ballColor = color(0);
    }

    //ボールを動かす処理
    //毎フレーム呼び出す必要あり
    //引数
    //_racket_center : ラケットの中心のX座標
    //_racket_y      : ラケットのY座標　当たり判定はラケット上部の高さ1pxのみ
    //_racket_witdh  : ラケットの幅 
    //すべてウィンドウに対する座標ではなく場を基準とした座標

    //返り値の説明
    // -1: ボールの移動が許可されていない
    // 0 : 通常
    // 1 : ボールが下に落ちた
    // 2 : ボールがラケットに当たった
    public int ballMove(int _racket_center, int _racket_y, int _racket_witdh){
        if(!moveEnable){
            //移動が許可されてないなら終了
            return -1;
        }

        //返り値用変数
        int result = 0;

        //ボールの移動
        x += speed_x;
        y += speed_y;


        //壁の反射処理
        if((int)y <= 0){ 
            //上の壁に当たった
            if(speed_y < 0){
                speed_y *= -1.0f;
                now_angle = 360.0f - now_angle;
            }
        }else if((int)y >= space.s_height){    
            //下に落ちた
            return 1;
        }

        if((int)x <= 0){
            //左の壁に当たった
            if(speed_x < 0.0f){
                speed_x *= -1.0f;
                now_angle = 180.0f - now_angle;
                if(now_angle < 0.0f){
                    now_angle += 360.0f;
                }
            }
        }else if((int)x + b_size >= space.s_width){
            //右の壁に当たった
            if(speed_x > 0.0f){
                speed_x *= -1.0f;
                now_angle = 180 - now_angle;
                if(now_angle < 0.0f){
                    now_angle += 360.0f;
                }
            }
        }

        //ラケットに当たっているかの判定
        if(y <=_racket_y && y + b_size > _racket_y){
            //ラケットの端の座標を計算
            int racket_left  = _racket_center - (_racket_witdh / 2);
            int racket_right = _racket_center + (_racket_witdh / 2);
            if(x + b_size > racket_left && x <= racket_right){
                //ボールの跳ね返し
                result = 2;
                //ラケットの当たった場所によって角度を変更
                float posi = (racket_right - (x + (b_size / 2.0f))) / _racket_witdh;
                if(posi <= 0 ){ 
                    posi = 0;
                }else if(posi >= 1){
                    posi = 1;
                }
                setSpeedWithAngle(speed, posi * 120 + 30);
            }
        }

        //ブロックに当たっているかの判定
        //ボールを四角形としたときの四隅を見る

        //ボールの端の座標
        int left  = constrain((int)x, 0, space.s_width - 1);             
        int right = constrain((int)x + b_size - 1 , 0, space.s_width - 1);
        int up    = constrain((int)y, 0, space.s_height - 1);
        int down  = constrain((int)y + b_size - 1, 0, space.s_height - 1);

        //ブロックとぶつかっているところを検出
        int collisionflag = 0;
        if(fieldData[up][left] >= 0){
            collisionflag |= 0x8;
        }
        if(fieldData[up][right] >= 0){
            collisionflag |= 0x4;
        }
        if(fieldData[down][left] >= 0){
            collisionflag |= 0x2;
        }
        if(fieldData[down][right] >= 0){
            collisionflag |= 0x1;
        }
        
        //ボールの反射
        switch(collisionData[collisionflag]){
            case 0:     //何もしない
                
                break;
            
            case 1:     //X軸方向に反射
                speed_x = speed_x * -1;
                break;

            case 2:     //Y軸方向に反射
                speed_y = speed_y * -1;
                break;
            
            case 3:     //XY軸方向に反射
                speed_x = speed_x * -1;
                speed_y = speed_y * -1;

                break;
            
            case 4:     //左上があたった
                if(90 <= now_angle && now_angle <= 180){    
                    //角に跳ね返される動き
                    float temp = -speed_x;
                    speed_x = -speed_y;
                    speed_y = temp;
                    
                }else if(now_angle <= 90){  //反射する動き
                    speed_y *= -1;
                }else{
                    speed_x *= -1;
                }

                break;

            case 5:     //右上が当たった
                if(0 <= now_angle && now_angle <= 90){    
                    //角に跳ね返される動き
                    float temp = speed_x;
                    speed_x = speed_y;
                    speed_y = temp;
                }else if(now_angle <= 180){  //反射する動き
                    speed_y *= -1;
                }else{
                    speed_x *= -1;
                }

                break;

            case 6:     //左下が当たった
                if(180 <= now_angle && now_angle <= 270){    
                    //角に跳ね返される動き
                    float temp = speed_x;
                    speed_x = speed_y;
                    speed_y = temp;
                }else if(270 <= now_angle){  //反射する動き
                    speed_y *= -1;
                }else{
                    speed_x *= -1;
                }

                break;

            case 7:     //右下が当たった
                if(270 <= now_angle && now_angle <= 360){    
                    //角に跳ね返される動き
                    float temp = -speed_x;
                    speed_x = -speed_y;
                    speed_y = temp;
                }else if(180 >= now_angle){  //反射する動き
                    speed_y *= -1;
                }else{
                    speed_x *= -1;
                }

                break;

            default :   //何もしない
                
                break;	
                
        }
        //now_angleの再計算
        calcAngle();

        //ブロックの消去処理
        if(fieldData[up][left] >= 0){
            block[fieldData[up][left]].collision(1);
        }
        if(fieldData[up][right] >= 0){
            block[fieldData[up][right]].collision(1);
        }
        if(fieldData[down][left] >= 0){
            block[fieldData[down][left]].collision(1);
        }
        if(fieldData[down][right] >= 0){
            block[fieldData[down][right]].collision(1);
        }
        
        return result;
    }

    //ボールの描写
    public void ballDraw(){
        fill(ballColor);
        noStroke();
        circle(space.s_x + (int)x + (b_size / 2), space.s_y + (int)y + (b_size / 2), b_size);
        stroke(0);
    }


    //ボールのスピードを変更する
    //XY軸それぞれで指定
    public void setSpeed(float _x, float _y){
        speed_x = _x;
        speed_y = _y;
    }

    //ボールのスピードを変更する
    //speed=ボールの速さ
    //角度は度数法で指定
    public void setSpeedWithAngle(float _speed, float angle){
        now_angle = angle;
        //弧度法に変換
        float rad = radians(angle);

        //スピードの計算
        speed = _speed;
        speed_x = cos(rad) * speed;
        speed_y = sin(rad) * -speed;    //Y軸は向きが逆なので"-"をつける 
    }

    //現在のスピードから角度を再計算する
    public void calcAngle(){
        if(speed_x == 0){       // θ = 0, 180 の時
            if(speed_y >= 0){
                now_angle = 270;
            }else{
                now_angle = 90;
            }
        }else if(speed_x > 0){  // -90 < θ < 90 の時
            float rad = atan(-speed_y / speed_x);
            if(rad >= 0){       // 0 < θ < 90
                now_angle = degrees(rad);
            }else{              // -90 < θ < 0
                now_angle = degrees(rad) + 360.0f;
            }
        }else{                  // 90 < θ < 270の時
            float rad = atan(-speed_y / speed_x);
            now_angle = degrees(rad) + 180.0f;
        }
    }

    //ボールの位置を直接指定
    public void setPosition(int _x, int _y){
        x = _x;
        y = _y;
    }

    //ボールを動かす
    public void ballStart(){
        moveEnable = true;
    }
    //ボールを止める
    public void ballStop(){
        moveEnable = false;
    }

    //スピードを上げる
    public void addSpeed(float _speed){
        speed += _speed;
        setSpeedWithAngle(speed,now_angle);
    }
    
    //スピードを下げる
    public void subSpeed(float _speed){
        speed -= _speed;
        if(speed < 0){
            speed = 0;
        }
        setSpeedWithAngle(speed,now_angle);
    }

    //ボールの大きさを変更
    public void ballSizeChange(int _size){
        b_size = _size;
    }

    //ボールの色を変更
    public void ballColorChange(int c){
        ballColor = c;
    }
}
//ブロックについてのクラス
//コンストラクタ
//引数は(親クラス(this), ブロックの番号)

//最初にブロックの配列をつくる
//その後出現させるときにインスタンス化と処理を行う

//使い方
//初期化
//Block()

class Block{
    //親クラス
    PApplet parent;

    //ブロックの番号
    int blockNumber;

    //ブロックが画面上にあるかどうか
    boolean blockEnable = false;

    //ブロックの現在座標
    //フィールド座標
    int x, y;

    //ブロックの大きさ
    int b_width, b_height;


    //ブロックの固さ
    //ボールに当たるたび1づつ減る
    int weight;

    
    //コンストラクタ
    Block(PApplet _parent, int _blockNumber){
        parent = _parent;
        blockEnable = false;
        blockNumber = _blockNumber;
    }

    //ブロックの情報をセットする
    //引数は(X座標, Y座標, 横幅, 縦幅, 固さ)
    public void blockSet(int _x, int _y, int _width, int _height, int _weight){
        x = _x;
        y = _y;
        weight = _weight; 
        b_width = _width;
        b_height = _height;
        blockEnable = true;
    }

    //ブロックと衝突した時に呼び出される
    //引数は強さ
    public void collision(int power){
        weight -= power;
        if(weight <= 0){
            //ブロックを消す処理
            //fieldDataの更新
            for(int i = x ; i < x + b_width ; i++){
                for(int j = max(y, 0) ; j < y + b_height ; j++){
                    fieldData[j][i] = -1;
                }
            }

            //スコアの加算
            score += 100 + ((level - 1) * 10);

            //ブロックの生存フラグをおろす
            blockEnableList[blockNumber] = false;
            //ブロックへの参照を削除
            block[blockNumber] = null;
        }
    }

    //ブロックを1px下に移動
    public void blockMove(){
        //1px下に移動
        y++;

        //nextFieldDataの更新処理
        if( y <= 0 && y + b_height > 0){
            for(int i = x ; i < x + b_width ; i++){
                nextFieldData[i] = blockNumber;
            }
        }
    }

    //ブロックの描写
    public void blockDraw(){
        //画像データの表示
        image(blockImg[weight - 1], x + space.s_x, y + space.s_y);
    }
}
//弾
class Bullet{
    PApplet parent;
    int x;
    int y;
    int power;

    //引数(this, X座標, Y座標, 弾の強さ)
    Bullet(PApplet _parent, int _x, int _y, int _power){
        parent = _parent;
        x = _x;
        y = _y;
        power = _power;
    }

    //弾の移動
    //返り値は弾が消えるタイミングなら1
    //そうでなければ0
    public int move(){
        y -= 5;

        boolean destroyFlag = false;

        //上の壁に当たった場合
        if(y <= 0){
            return 1;
        }

        //衝突判定
        if(fieldData[y][x] != -1){
            block[fieldData[y][x]].collision(power);
            destroyFlag = true;
        }
        if(fieldData[y][x + 9] != -1){
            block[fieldData[y][x + 9]].collision(power);
            destroyFlag = true;
        }

        

        if(destroyFlag){
            return 1;
        }

        return 0;

    }

    //弾の描写
    public void draw(){
        image(bulletImg, space.s_x + x, y);
    }
}
//HSV関係の処理を行う
class HSVcheck{
    int H_low,H_high;
    int S_low,S_high;
    int V_low,V_high;

    //コンストラクタ
    //それぞれの範囲を256段階で指定
    //low <= x <= high で判定
    //Hのみ360段階
    HSVcheck(int h_l, int h_h, int s_l, int s_h, int v_l, int v_h){
        H_low  = h_l;
        H_high = h_h;
        S_low  = s_l;
        S_high = s_h;
        V_low  = v_l;
        V_high = v_h;
    }

    //範囲を変更
    public void rangeChenge(int h_l, int h_h, int s_l, int s_h, int v_l, int v_h){
        H_low  = h_l;
        H_high = h_h;
        S_low  = s_l;
        S_high = s_h;
        V_low  = v_l;
        V_high = v_h;
    }

    

    //HSV色空間で範囲の中にあるかチェック
    public boolean check(int c){
        //HSV色空間に変換
        int[] HSVdata = RGB2HSV(c >> 16 & 0xff, c >> 8 & 0xff, c & 0xff);

        if(H_high < H_low){
            //0度をまたぐ場合
            if(!(H_low <= HSVdata[0] || HSVdata[0] <= H_high)){
                return false;
            }
        }else{
            if(!(H_low <= HSVdata[0] && HSVdata[0] <= H_high)){
                return false;
            }
        }

        if(!(S_low <= HSVdata[1] && HSVdata[1] <= S_high)){
            return false;
        }
        
        if(!(V_low <= HSVdata[2] && HSVdata[2] <= V_high)){
            return false;
        }
        
        //すべて範囲内であるならtrue
        return true;
    }

    //RGBからHSVに変換
    public int[] RGB2HSV(float R, float G, float B){
        //返り値用
        int[] HSVdata = new int[3];

        float _max = max(R,G,B);
        float _min = min(R,G,B);
        
        HSVdata[2] = (int)_max;

        if(_max == 0){
            HSVdata[1] = 0;
            HSVdata[0] = 0;
        }else{
            float range = _max - _min;
            if(range == 0){
                HSVdata[1] = (int)0;
                HSVdata[0] = (int)0;
                return HSVdata;
            }

            HSVdata[1] = (int)(255 * (range / _max));

            if(_max == R){
                HSVdata[0] = (int)(60 * ((B - G) / range)); 
            }else if(_max == G){
                HSVdata[0] = (int)(60 * (2 + (R - B) / range));
            }else{
                HSVdata[0] = (int)(60 * (4 + (G - R) / range));
            }
            
            if(HSVdata[0] < 0){
                HSVdata[0] += 360;
            }
        }
        
        return HSVdata;
    }
}
//画像ファイルの読み込み
public void imageLoad(){
    //ブロックの画像データ読み込み  
    blockImg[0] = loadImage("Lv1_b.png");
    blockImg[1] = loadImage("Lv2_b.png");
    blockImg[2] = loadImage("Lv3_b.png");
    blockImg[3] = loadImage("Lv4_b.png");
    blockImg[4] = loadImage("Lv5_b.png");
    blockImg[5] = loadImage("Lv6_b.png");
    blockImg[6] = loadImage("Lv7_b.png");
    blockImg[7] = loadImage("Lv8_b.png");
    blockImg[8] = loadImage("Lv9_b.png");
    blockImg[9] = loadImage("Lv10_b.png");

    //ポインターの画像データ読み込み
    pointer = loadImage("pointer_25.png");

    //弾の画像データ読み込み
    bulletImg = loadImage("bullet.png");

    //矢印の画像データ読み込み
    arrowImg = loadImage("yazirusi.png");

}
//int型の値を二つ(x,y)もつクラス
//初期値は0
//主に関数の返り値に使う
class Vector2{
    int x;
    int y;

    Vector2(){
        x = 0;
        y = 0;
    }
}

//ボールが動ける範囲の定義を渡すためのクラス
//コンストラクタの引数
//(ボールが動き回れる場X,Y, 場の原点X,Y)
class SpaceData{
    //ボールが動き回れる幅
    int s_width, s_height;

    //ボールが動き回れる場所の左上の座標
    int s_x, s_y;

    SpaceData(int _space_width, int _space_height, int _space_x, int _space_y){
        s_width = _space_width;
        s_height = _space_height;
        s_x = _space_x;
        s_y = _space_y;
    }

    public void SpaceChange(int _space_width, int _space_height, int _space_x, int _space_y){
        s_width = _space_width;
        s_height = _space_height;
        s_x = _space_x;
        s_y = _space_y;
    }
}




//トラッキングに関する処理
class Tracking{

    //カメラからの画像を入れる
    Capture cap;

    //左右反転画像
    PImage inverseCap;

    //赤色のみを抽出し2値化したもの
    PImage redMat;
    //redMatからノイズを低減させたもの
    PImage effectedMat;

    //HSV変換をするためのクラス
    HSVcheck hsvCheck;

    //OpenCV
    OpenCV cv;

    //輪郭を一時的に保存するリスト
    //file:~省略~/Processing/libraries/opencv_processing/reference/gab/opencv/Contour.html
    ArrayList<Contour> contours;

    //検出する最小の面積
    float minArea = 1000;

    //コンストラクタ
    //初期設定等をすべて行う
    //引数はHSV変換のパラメータ
    //親を表すために最初にthisを入れて呼ぶ
    //赤色は(350,10,64,255,64,255)
    Tracking(PApplet parent, int H_low, int H_high, int S_low, int S_high, int V_low, int V_high){

        

        try{
            //利用可能なカメラを取得
            String[] cams = Capture.list();
            //printArray(cams);


            if(cams.length == 0){
                println("カメラが検出されませんでした");
                exit();
            }

            //int camNum = 0;

            //使用できるカメラ番号を検出
            /*for(int i = 0 ; i < cams.length ; i++){
                String[] temp = split(cams[i], ",");
                //println(temp[0]);
                //println(temp[1]);
                //println(temp[2]);
                
                if(temp[1] == "size=640x360" || (temp[2] == "fps=30" || temp[2] == "fps=60")){
                    camNum = i;
                    println(i);
                }
            }*/
            

            //capの初期化
            //cap = new Capture(parent, 640, 360, 30, cams[camNum]);
            cap = new Capture(parent, 640, 360, 30);        //カメラ名を使用しない方法　
            cap.start();

        }catch (Exception e) {
            println("カメラ関係でエラーが起きました");
            exit();
        }

        //PImageの初期化
        redMat = new PImage(640, 360);
        effectedMat = new PImage(640, 360);
        inverseCap = new PImage(640, 360);

        for(int i = 0 ; i < 230400 ; i++){
            redMat.pixels[i] = color(0);
        }

        //HSV変換を呼び出す
        hsvCheck = new HSVcheck(H_low, H_high, S_low, S_high, V_low, V_high);

        //openCVの初期化
        cv = new OpenCV(parent, redMat);
    }

    CamPosition pre_position = new CamPosition(0,0,false);

    //追跡したものの座標
    public CamPosition LoadPosition(){
        if(cap.available()){

            //カメラ映像の取得
            cap.read();

            //反転画像の作成
            inverseCap.loadPixels();
            for(int i = 0 ; i < 360 ; i++){
                for(int j = 0 ; j < 640 ; j++){
                    inverseCap.pixels[i * 640 + j] = cap.pixels[i * 640 + (639 -j)];
                }
            }
            inverseCap.updatePixels();


            //赤色の抽出
            redMat.loadPixels();    //これしないとエラー起こす
            int tempColor;
            for(int i = 0 ; i < 230400 ; i++){
                tempColor = inverseCap.pixels[i];
                if(hsvCheck.check(tempColor)){
                    redMat.pixels[i] = color(255);
                }else{
                    redMat.pixels[i] = color(0);
                }
            }
            redMat.updatePixels();  //同じく

            //openCVへ読み込み
            cv.loadImage(redMat);

            //ノイズ低減処理
            cv.erode();
            cv.erode();
            cv.dilate();
            cv.dilate();

            //輪郭抽出
            //最大の輪郭をmaxContourに代入
            contours = cv.findContours();
            float max = minArea;
            Contour maxContour = null;
            for(Contour contour : contours){
                if(max <= contour.area()){
                    max = contour.area();
                    maxContour = contour;
                }
            }

            //輪郭から座標を計算
            if(maxContour != null){
                //中心座標の計算
                Rectangle rectangle = maxContour.getBoundingBox();
                int center_x = rectangle.x + (rectangle.width / 2);
                int center_y = rectangle.y + (rectangle.height / 2);
                
                //返り値の作成
                CamPosition position = new CamPosition(center_x, center_y, true);
                pre_position = position;
                return position;
            }
        }
        
        //返り値の作成
        pre_position.tracked = false;
        return pre_position;

    }

    //色を検出する最小の面積の変更
    public void minAreaChange(float newMinArea){
        minArea = newMinArea;
    }
}

//二次元座標の受け渡しに使う
class CamPosition{
    int x;
    int y;
    boolean tracked;

    CamPosition(int _x, int _y, boolean _tracked){
        x = _x;
        y = _y;
        tracked = _tracked;
    }
}
//フレームのカウントに使う(共通)
int start_frame;

//関数に初めて入ったタイミングの処理をするための変数
//デフォルト true
//関数内でtrueだったら　初期化処理＆falseに
//modeを変更するときにtrueに戻す
boolean menu2game_flag = true;
boolean gameoverAnimation_flag = true;
boolean over2menu_flag = true;


//メニュー画面からゲーム画面へ
// Are you ready? って出す
//60フレーム
public void menu2game(){
    if(menu2game_flag){
        menu2game_flag = false;
        //初期化処理
        start_frame = frameCount;
    }
    int now_frame = frameCount - start_frame;

    //スクワットのカウントを無効化
    squat_flag = false;


    //文字の表示
    textSize(60);
    noStroke();

    if(now_frame <= 9){
        //最初10フレーム
        //文字が現れる
        fill(0,25 * now_frame);
        text("Are You Ready?", space.s_x + 28, 80 + now_frame * 5);
    }else if(now_frame <= 49){
        //次の40フレーム
        //文字は固定
        fill(0);
        text("Are You Ready?", space.s_x + 28, 130);
    }else{
        //最後10フレーム
        //文字は下に消えていく
        fill(0, 255 - 25 * (now_frame - 49));
        text("Are You Ready?", space.s_x + 28, 130 + (now_frame - 49) * 5);
    }


    //終了処理
    if(now_frame >= 59){
        menu2game_flag = true;

        mode = 1;           //ゲームスタート
        squat_flag = true;  //スクワットのカウントを有効化

        //ブロックの作成
        blockCreate();

        //ボール追加
        ball.add(new Ball(this, racket_center, space.s_y + space.s_height - 130));
        ball.get(ball.size() - 1).setSpeedWithAngle(5,60);
        ball.get(ball.size() - 1).ballStart();

        //ラケットの移動量計算の初期化
        pre_racket_center = racket_center;

        //ボール発射SEを鳴らす
        addBall.trigger();
    }
}

//ゲームオーバーになった瞬間のアニメーション
// GameOver って出す
//180フレーム
//これは呼び出すタイミングが一番最後
//跳ね返りを実現するための変数
int dy = 5;    //Y軸方向の速度
int y = 0;      //文字列の下の座標
public void gameoverAnimation(){
    if(gameoverAnimation_flag){
        gameoverAnimation_flag = false;
        //初期化処理
        start_frame = frameCount;

        dy = 5;
        y = 0;
    }
    int now_frame = frameCount - start_frame;

    //スクワットのカウントを無効化
    squat_flag = false;

    noStroke();
    //テキストなどの描写
    if(now_frame <= 59){
        //テキストと背景の描写
        fill(128);
        rect(space.s_x + 30, y - 80, 440, 100, 10);
        fill(0);
        textSize(80);
        text("GameOver", space.s_x + 42, y);

        //跳ね返るようなアニメーション
        y += dy;
        dy += 2;
        if(y >= 300){
            y = 300;
            dy *= -0.5f;
        }
    }else if(now_frame <= 169){
        //ラスト10フレームまで
        fill(128);
        rect(space.s_x + 30, 220, 440, 100, 10);
        fill(0);
        textSize(80);
        text("GameOver", space.s_x + 42, 300);
    }else{
        //ラスト10フレーム
        //下に消える
        fill(128, 250 - (now_frame - 169) * 25);
        rect(space.s_x + 30, 220 + (now_frame - 169) * 5, 440, 100, 10);
        fill(0, 250 - (now_frame - 169) * 25);
        textSize(80);
        text("GameOver", space.s_x + 42, 300 + (now_frame - 169) * 5);
    }


    //終了処理
    if(now_frame >= 179){
        gameoverAnimation_flag = true;

        mode = 2;           //ゲームオーバー画面に
        squat_flag = false; //スクワットのカウントを無効化(すぐに選択されないようにするため)
    }
}

//ゲームオーバー画面からメニュー画面に戻る
//円弧を描いて灰色におおわれた後、円弧を描き見えるようにする
//これは呼び出すタイミングが一番最後
//120フレーム

//確実に初期化するための管理フラグ
boolean init_flag = false;
public void over2menu(){
    if(over2menu_flag){
        over2menu_flag = false;
        //初期化処理
        start_frame = frameCount;
    }
    int now_frame = frameCount - start_frame;

    //スクワットのカウントを無効化
    squat_flag = false;

    //カメラオンならカメラを表示する
    //背景とする
    if(cameraEnable){
        image(tracking.inverseCap, 290, 200, 500, 280);
        noStroke();
        fill(255);
        rect(space.s_x, 480, 500, 60);
        
        //カメラを半透明にする
        noStroke();
        fill(255, 255, 255, 128);   //要調整
        rect(290, 200, 500, 280);
    }

    noStroke();
    fill(200);
    if(now_frame <= 59){
        //最初の30フレーム
        //円弧が覆う
        arc(400, 350, 2000, 2000, radians(-90), radians(-90 + now_frame * 6));
    }else if(init_flag == false){
        //初期化
        init();
        init_flag = true;
        rect(0,0,800,700);
    }else{
        //最後の30フレーム
        //円弧が消えていく
        arc(400, 350, 2000, 2000, radians(-90 + (now_frame - 60) * 6), radians(270));
    }


    //終了処理
    if(now_frame >= 119){
        over2menu_flag = true;
        init_flag = false;

        mode = 0;           //メニュー画面に
        squat_flag = false; //スクワットのカウントを無効化(すぐに選択されないようにするため)
    }
}




//サウンド関係

Minim minim;
AudioOutput out;
Sampler are_you_ready, gameover, bound, hassya, addBall;

//サウンド関係の初期化
public void soundInit(){
    minim = new Minim(this);
    out = minim.getLineOut();

    //音声データの読み込み
    are_you_ready = new Sampler("are you ready.wav", 1, minim);
    gameover = new Sampler("gameover.wav", 1, minim);
    bound = new Sampler("hane.wav", 10, minim);
    hassya = new Sampler("bullet.wav", 10, minim);
    addBall = new Sampler("balladd.wav", 3, minim);

    //出力準備
    are_you_ready.patch(out);
    gameover.patch(out);
    bound.patch(out);
    hassya.patch(out);
    addBall.patch(out);
}
//何フレームでブロックを動かすかの設定
int speed = 5;

//インクリメントしていくやつ
int now_count = 0;

//何px進んだか
int px_cnt = 0;

//何パターン出現したか 3パターンごとにリセットしlevelを1上げる
int pt_cnt = 0;

//現在のレベル 3パターン出現ごとに1上げる
//ハードモードは5スタート
int level = 1;

//ブロックパターンの数
int block_pattern_count = 5;

//ブロックの出現パターン
//10x15のパターンが十種類ありランダムで出現
//level値を加算していく
int[][][] blockPattern = new int[block_pattern_count][15][10];


//ゲーム中の処理
public void game(){

    //ボールをすべて落とした時のペナルティ
    if(ball.size() == 0){
        now_count += 2;
    }

    //ブロックの移動チェック
    now_count++;
    if(now_count >= speed){
        now_count = 0;
        px_cnt++;
        
        blockMoveEnable = true;
    }else{
        blockMoveEnable = false;
    }

    //ブロックの移動　　ボールの移動より先に書く
    if(blockMoveEnable){   //今は毎フレーム下げてるからtrue
        //nextFieldDataの初期化
        for(int i = 0 ; i < 500 ; i++){
            nextFieldData[i] = -1;
        }

        //ブロックの移動
        for(int i = 0 ; i < max_block_count ; i++){
            if(blockEnableList[i]){
                block[i].blockMove();
            }
        }
        //ブロックの移動に合わせてフィールドデータを更新
        fieldMove();
        

        //次のパターンの出現
        if(px_cnt >= 320){
            px_cnt -= 320;
            blockCreate();
        }

        //この中にブロックが一番下まで下がったかの判定を書く
        boolean gameoverFlag = false;
        for(int i = 0 ; i < 500 ; i++){
            if(fieldData[579][i] != -1){
                gameoverFlag = true;
                break;
            }
        }

        if(gameoverFlag){
            //ゲームオーバー
            blockMoveEnable = false;
            mode = 4;   //ゲームオーバーアニメーションに遷移
            squat_flag = false;
            squat_frame = 0;
            ball.clear();
            bullet.clear();

            //消費カロリー情報の書き込み
            kcalWrite();

            //SE再生
            gameover.trigger();
        }
    }

    //ボールの移動
    for(int i = 0 ; i < ball.size() ; i++){
        int result = ball.get(i).ballMove(racket_center, 580, racket_width); 
        if(result == 1){
            //ボールが落下
            ball.remove(i);
        }else if(result == 2){
            //反射SEを鳴らす
            bound.trigger();
        }
    }

    //弾の移動
    for(int i = 0 ; i < bullet.size() ; i++){
        int result = bullet.get(i).move();
        if(result == 1){
            //弾が消える
            bullet.remove(i);
        }
    }

    //カロリー計算
    six_frame_count++;
    if(six_frame_count >= 6){
        six_frame_count -= 6;
        int move_length = abs(pre_racket_center - racket_center);
        kcal += move_length / 30000.0f;
        today_kcal += move_length / 30000.0f;

        pre_racket_center = racket_center;
    }
}

//ブロックパターンの読み込み
public void patternLoad(){
    
    for(int i = 0 ; i < block_pattern_count ; i++){
        String[] txtData = loadStrings("BlockPattern" + str(i + 1) + ".txt");
        for(int j = 0 ; j < 15 ; j++){
            int[] temp = PApplet.parseInt(split(txtData[j], ","));
            for(int k = 0 ; k < 10 ; k++){
                blockPattern[i][j][k] = temp[k];
            }
        }
    }
}

//ブロックの配置　次フレームから出現する位置に配置
public void blockCreate(){
    int patternNum = PApplet.parseInt(random(block_pattern_count));

    for(int i = 0 ; i < 15 ; i++){
        for(int j = 0 ; j < 10 ; j++){
            if(blockPattern[patternNum][i][j] + min(level, 10) - 1 >= 1){
                block[nextBlockNum] = new Block(this, nextBlockNum);
                blockEnableList[nextBlockNum] = true;
                block[nextBlockNum].blockSet(j * 50, -300 + (i * 20), 50, 20, min(blockPattern[patternNum][i][j] + min(level, 10) - 1, 10));
                nextBlockNum++;
                if(nextBlockNum >= max_block_count){
                    nextBlockNum = 0;
                }
            }
        }
    }

    pt_cnt++;
    if(pt_cnt >= 3){
        pt_cnt -= 3;
        level++;
    }

}
//ゲームオーバー画面
public void gameOver(){
    boolean countup_flag = false;;

    //ポインター処理
    if(pointer01 == 1){
        if(racket_center <= 150){
            countup_flag = true;
        }else{
            squat_frame = 0;
        }
    }


    //カメラオンならカメラを表示する
    //背景とする
    if(cameraEnable){
        image(tracking.inverseCap, 290, 200, 500, 280);
        noStroke();
        fill(255);
        rect(space.s_x, 480, 500, 60);
        
        //カメラを半透明にする
        noStroke();
        fill(255, 255, 255, 128);   //要調整
        rect(290, 200, 500, 280);
    }


    //ポインター表示
    image(arrowImg, space.s_x + racket_center - 50, 300 + pointer_y * 1.2f, 100, 100);

    //モード表示
    image(blockImg[0], space.s_x, 480, 150, 60);
    textSize(30);
    noStroke();
    fill(0);
    text("メニューへ", space.s_x + 20, 520);

    //カウントアップ
    if(countup_flag && squat_flag){
        noStroke();
        fill(128,128);
        arc(space.s_x + (space.s_width / 2), 520, 80, 80, radians(-90), radians(-90 + squat_frame * 6));
        textSize(30);
        fill(0);
        text("OK?", space.s_x + 220, 530);
    }

    //画面切り替えアニメーションへ
    if(squat_frame >= 60){
        mode = 5;
        squat_frame = 0;
        squat_flag = false;
    }
}
//カロリー計算のデータ関係

//消費カロリーの読み込み
public void kcalLoad(){
    String[] txtData = loadStrings("data/kcalText.txt");
    if(PApplet.parseInt(txtData[0]) == month() && PApplet.parseInt(txtData[1]) == day()){
        //データと同じ日ならtoday_kcalを更新
        today_kcal = PApplet.parseFloat(txtData[2]);
    }else{
        today_kcal = 0;
    }
}

//消費カロリーの書き込み
public void kcalWrite(){
    String[] txtData = new String[3];
    txtData[0] = str(month());
    txtData[1] = str(day());
    txtData[2] = str(today_kcal);
    saveStrings("data/kcalText.txt", txtData);
}
//メニュー画面
//難易度選択
public void menu(){
    
    boolean countup_flag = false;

    //テキスト表示
    textSize(45);
    noStroke();
    fill(0);
    text("難易度を選択してください", space.s_x + 20, 190);

    //ポインター処理
    if(pointer01 == 1){
        if(racket_center <= 150 || 350 <= racket_center){
            countup_flag = true;
        }else{
            squat_frame = 0;
        }
    }

    //ポインター表示
    image(arrowImg, space.s_x + racket_center - 50, 300 + pointer_y * 1.2f, 100, 100);

    //モード表示
    image(blockImg[0], space.s_x, 480, 150, 60);
    image(blockImg[9], space.s_x + 350, 480, 150, 60);
    textSize(30);
    noStroke();
    fill(0);
    text("ノーマル", space.s_x + 28, 520);
    text("ハード"  , space.s_x + 388, 520);

    //カウントアップ
    if(countup_flag && squat_flag){
        noStroke();
        fill(128,128);
        arc(space.s_x + (space.s_width / 2), 520, 80, 80, radians(-90), radians(-90 + squat_frame * 6));
        textSize(30);
        fill(0);
        text("OK?", space.s_x + 220, 530);
    }

    //ゲーム開始アニメーションへ
    if(squat_frame >= 60){
        mode = 3;
        squat_frame = 0;
        squat_flag = false;

        //レベル決定
        if(racket_center <= 150){
            level = 1;
        }else{
            level = 5;
        }

        //SE再生
        are_you_ready.trigger();
    }
}
  public void settings() {  size(800,700); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "BlockBreaking" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
