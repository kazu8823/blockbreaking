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
 
void setup() {

    //フィールドの設定
    space = new SpaceData(500,680,290,10);

    //サイズの設定
    size(800,700);

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

void draw() {
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
                kcal += squat_frame / 720.0;
                today_kcal += squat_frame / 720.0;

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
            circle(racket_center + 290.5, space.s_y + 580 + pointer_y + 12.5, 40);
            fill(150);
            arc(racket_center + 290.5, space.s_y + 580 + pointer_y + 12.5, 40, 40, radians(-90), radians(-90 + squat_frame * 3));
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
void fieldMove(){
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
void init(){

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
