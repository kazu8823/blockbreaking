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

    void SpaceChange(int _space_width, int _space_height, int _space_x, int _space_y){
        s_width = _space_width;
        s_height = _space_height;
        s_x = _space_x;
        s_y = _space_y;
    }
}
