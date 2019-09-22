package verityfy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Test {
    public void test(int a,int b,int c){
        if(a<b){
            c=a;
            a++;
            int d=10,f=4;
            System.out.println(a);
            test(a,d,f);
            return;
        }
    }

    public static void main(String[] args) throws IOException {
        Test test=new Test();
//        test.test(1,2,3);

        String s="1234545";
        String b="34545";
        System.out.println(s.indexOf(b));

        String filePath="E:\\files\\file";
        Map<String, List<String>> stateBuffer=new LinkedHashMap<>();
        List<String> lis=new ArrayList<>();
        lis.add("s");
        lis.add("v");
        stateBuffer.put("s",lis);
        stateBuffer.put("d",lis);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(filePath, true);
            if(stateBuffer!=null) {
                for (String key : stateBuffer.keySet()) {
                    fos.write(key.getBytes());
                    fos.write("\r\n".getBytes());
                    List<String> list = stateBuffer.get(key);
                    for (String str : list) {
                        fos.write(str.getBytes());
                        fos.write("\r\n".getBytes());
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            if(fos != null){
                try {
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }
}
