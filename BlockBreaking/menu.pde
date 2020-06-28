//メニュー画面
//難易度選択
void menu(){
    
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
    image(arrowImg, space.s_x + racket_center - 50, 300 + pointer_y * 1.2, 100, 100);

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

