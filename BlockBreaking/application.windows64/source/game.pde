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
void game(){

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
        kcal += move_length / 30000.0;
        today_kcal += move_length / 30000.0;

        pre_racket_center = racket_center;
    }
}

//ブロックパターンの読み込み
void patternLoad(){
    
    for(int i = 0 ; i < block_pattern_count ; i++){
        String[] txtData = loadStrings("BlockPattern" + str(i + 1) + ".txt");
        for(int j = 0 ; j < 15 ; j++){
            int[] temp = int(split(txtData[j], ","));
            for(int k = 0 ; k < 10 ; k++){
                blockPattern[i][j][k] = temp[k];
            }
        }
    }
}

//ブロックの配置　次フレームから出現する位置に配置
void blockCreate(){
    int patternNum = int(random(block_pattern_count));

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
