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
void menu2game(){
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
void gameoverAnimation(){
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
            dy *= -0.5;
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
void over2menu(){
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
