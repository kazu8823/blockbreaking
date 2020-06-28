import ddf.minim.*;
import ddf.minim.ugens.*;


//サウンド関係

Minim minim;
AudioOutput out;
Sampler are_you_ready, gameover, bound, hassya, addBall;

//サウンド関係の初期化
void soundInit(){
    minim = new Minim(this);
    out = minim.getLineOut();

    //音声データの読み込み
    are_you_ready = new Sampler("are you ready.wav", 1, minim);
    gameover = new Sampler("gameover.wav", 1, minim);
    bound = new Sampler("hane.wav", 10, minim);
    hassya = new Sampler("bullet.wav", 10, minim);
    addBall = new Sampler("balladd.wav", 3, minim);

    //出力準備
    are_you_ready.patch(out);
    gameover.patch(out);
    bound.patch(out);
    hassya.patch(out);
    addBall.patch(out);
}