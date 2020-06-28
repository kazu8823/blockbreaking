//ゲームオーバー画面
void gameOver(){
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
    image(arrowImg, space.s_x + racket_center - 50, 300 + pointer_y * 1.2, 100, 100);

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