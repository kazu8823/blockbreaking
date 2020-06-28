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
    int move(){
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
    void draw(){
        image(bulletImg, space.s_x + x, y);
    }
}
