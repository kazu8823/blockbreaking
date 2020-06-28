//カロリー計算のデータ関係

//消費カロリーの読み込み
void kcalLoad(){
    String[] txtData = loadStrings("data/kcalText.txt");
    if(int(txtData[0]) == month() && int(txtData[1]) == day()){
        //データと同じ日ならtoday_kcalを更新
        today_kcal = float(txtData[2]);
    }else{
        today_kcal = 0;
    }
}

//消費カロリーの書き込み
void kcalWrite(){
    String[] txtData = new String[3];
    txtData[0] = str(month());
    txtData[1] = str(day());
    txtData[2] = str(today_kcal);
    saveStrings("data/kcalText.txt", txtData);
}
