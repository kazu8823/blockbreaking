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
    void blockSet(int _x, int _y, int _width, int _height, int _weight){
        x = _x;
        y = _y;
        weight = _weight; 
        b_width = _width;
        b_height = _height;
        blockEnable = true;
    }

    //ブロックと衝突した時に呼び出される
    //引数は強さ
    void collision(int power){
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
    void blockMove(){
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
    void blockDraw(){
        //画像データの表示
        image(blockImg[weight - 1], x + space.s_x, y + space.s_y);
    }
}