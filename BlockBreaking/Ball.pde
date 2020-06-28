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
    color ballColor;

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
    int ballMove(int _racket_center, int _racket_y, int _racket_witdh){
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
                speed_y *= -1.0;
                now_angle = 360.0 - now_angle;
            }
        }else if((int)y >= space.s_height){    
            //下に落ちた
            return 1;
        }

        if((int)x <= 0){
            //左の壁に当たった
            if(speed_x < 0.0){
                speed_x *= -1.0;
                now_angle = 180.0 - now_angle;
                if(now_angle < 0.0){
                    now_angle += 360.0;
                }
            }
        }else if((int)x + b_size >= space.s_width){
            //右の壁に当たった
            if(speed_x > 0.0){
                speed_x *= -1.0;
                now_angle = 180 - now_angle;
                if(now_angle < 0.0){
                    now_angle += 360.0;
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
                float posi = (racket_right - (x + (b_size / 2.0))) / _racket_witdh;
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
    void ballDraw(){
        fill(ballColor);
        noStroke();
        circle(space.s_x + (int)x + (b_size / 2), space.s_y + (int)y + (b_size / 2), b_size);
        stroke(0);
    }


    //ボールのスピードを変更する
    //XY軸それぞれで指定
    void setSpeed(float _x, float _y){
        speed_x = _x;
        speed_y = _y;
    }

    //ボールのスピードを変更する
    //speed=ボールの速さ
    //角度は度数法で指定
    void setSpeedWithAngle(float _speed, float angle){
        now_angle = angle;
        //弧度法に変換
        float rad = radians(angle);

        //スピードの計算
        speed = _speed;
        speed_x = cos(rad) * speed;
        speed_y = sin(rad) * -speed;    //Y軸は向きが逆なので"-"をつける 
    }

    //現在のスピードから角度を再計算する
    void calcAngle(){
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
                now_angle = degrees(rad) + 360.0;
            }
        }else{                  // 90 < θ < 270の時
            float rad = atan(-speed_y / speed_x);
            now_angle = degrees(rad) + 180.0;
        }
    }

    //ボールの位置を直接指定
    void setPosition(int _x, int _y){
        x = _x;
        y = _y;
    }

    //ボールを動かす
    void ballStart(){
        moveEnable = true;
    }
    //ボールを止める
    void ballStop(){
        moveEnable = false;
    }

    //スピードを上げる
    void addSpeed(float _speed){
        speed += _speed;
        setSpeedWithAngle(speed,now_angle);
    }
    
    //スピードを下げる
    void subSpeed(float _speed){
        speed -= _speed;
        if(speed < 0){
            speed = 0;
        }
        setSpeedWithAngle(speed,now_angle);
    }

    //ボールの大きさを変更
    void ballSizeChange(int _size){
        b_size = _size;
    }

    //ボールの色を変更
    void ballColorChange(color c){
        ballColor = c;
    }
}