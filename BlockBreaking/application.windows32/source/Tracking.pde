import processing.video.*;
import gab.opencv.*;
import java.awt.Rectangle;

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
    CamPosition LoadPosition(){
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
    void minAreaChange(float newMinArea){
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
