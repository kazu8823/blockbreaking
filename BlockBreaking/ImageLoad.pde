//画像ファイルの読み込み
void imageLoad(){
    //ブロックの画像データ読み込み  
    blockImg[0] = loadImage("Lv1_b.png");
    blockImg[1] = loadImage("Lv2_b.png");
    blockImg[2] = loadImage("Lv3_b.png");
    blockImg[3] = loadImage("Lv4_b.png");
    blockImg[4] = loadImage("Lv5_b.png");
    blockImg[5] = loadImage("Lv6_b.png");
    blockImg[6] = loadImage("Lv7_b.png");
    blockImg[7] = loadImage("Lv8_b.png");
    blockImg[8] = loadImage("Lv9_b.png");
    blockImg[9] = loadImage("Lv10_b.png");

    //ポインターの画像データ読み込み
    pointer = loadImage("pointer_25.png");

    //弾の画像データ読み込み
    bulletImg = loadImage("bullet.png");

    //矢印の画像データ読み込み
    arrowImg = loadImage("yazirusi.png");

}