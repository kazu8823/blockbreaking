//HSV関係の処理を行う
class HSVcheck{
    int H_low,H_high;
    int S_low,S_high;
    int V_low,V_high;

    //コンストラクタ
    //それぞれの範囲を256段階で指定
    //low <= x <= high で判定
    //Hのみ360段階
    HSVcheck(int h_l, int h_h, int s_l, int s_h, int v_l, int v_h){
        H_low  = h_l;
        H_high = h_h;
        S_low  = s_l;
        S_high = s_h;
        V_low  = v_l;
        V_high = v_h;
    }

    //範囲を変更
    void rangeChenge(int h_l, int h_h, int s_l, int s_h, int v_l, int v_h){
        H_low  = h_l;
        H_high = h_h;
        S_low  = s_l;
        S_high = s_h;
        V_low  = v_l;
        V_high = v_h;
    }

    

    //HSV色空間で範囲の中にあるかチェック
    boolean check(int c){
        //HSV色空間に変換
        int[] HSVdata = RGB2HSV(c >> 16 & 0xff, c >> 8 & 0xff, c & 0xff);

        if(H_high < H_low){
            //0度をまたぐ場合
            if(!(H_low <= HSVdata[0] || HSVdata[0] <= H_high)){
                return false;
            }
        }else{
            if(!(H_low <= HSVdata[0] && HSVdata[0] <= H_high)){
                return false;
            }
        }

        if(!(S_low <= HSVdata[1] && HSVdata[1] <= S_high)){
            return false;
        }
        
        if(!(V_low <= HSVdata[2] && HSVdata[2] <= V_high)){
            return false;
        }
        
        //すべて範囲内であるならtrue
        return true;
    }

    //RGBからHSVに変換
    int[] RGB2HSV(float R, float G, float B){
        //返り値用
        int[] HSVdata = new int[3];

        float _max = max(R,G,B);
        float _min = min(R,G,B);
        
        HSVdata[2] = (int)_max;

        if(_max == 0){
            HSVdata[1] = 0;
            HSVdata[0] = 0;
        }else{
            float range = _max - _min;
            if(range == 0){
                HSVdata[1] = (int)0;
                HSVdata[0] = (int)0;
                return HSVdata;
            }

            HSVdata[1] = (int)(255 * (range / _max));

            if(_max == R){
                HSVdata[0] = (int)(60 * ((B - G) / range)); 
            }else if(_max == G){
                HSVdata[0] = (int)(60 * (2 + (R - B) / range));
            }else{
                HSVdata[0] = (int)(60 * (4 + (G - R) / range));
            }
            
            if(HSVdata[0] < 0){
                HSVdata[0] += 360;
            }
        }
        
        return HSVdata;
    }
}
