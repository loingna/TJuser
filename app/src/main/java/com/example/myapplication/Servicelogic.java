package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.dao.DataInteraction;
import com.example.myapplication.dao.EncAndDec;
import com.example.myapplication.utils.CreateKey;
import com.example.myapplication.utils.FileReadload;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Servicelogic extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG="主界面";
    private static final String TAG2="发送线程";
    private static final String TAG3="接收线程";
    private static final int BLOCK_SIZE = 16; // 分块大小为16字节



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_servicelogic);

        TextView usernames = findViewById(R.id.textView3);
        Spinner showencrpfile=findViewById(R.id.choessfile);
        Spinner showsendfile=findViewById(R.id.sendfile);
        Spinner showneeddecrp=findViewById(R.id.showneeddecrp);
        Spinner showkeyenvelop=findViewById(R.id.showkeyenvelop);
        // 获取当前所有文件
        File file = new File(getFilesDir().getPath());
        String[] fileList = file.list();

        //获取从登陆端传输的用户名信息
        SharedPreferences sps=getSharedPreferences("newusers",MODE_PRIVATE);
        String newuser=sps.getString("newuser","error");
        usernames.append(newuser);
        // 申请一个列表用于保存添加的item
        if(fileList != null){
            ArrayList<String> items = new ArrayList<>(Arrays.asList(fileList));
            // 将item添加到spinner组件中
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            showencrpfile.setAdapter(adapter);
            showsendfile.setAdapter(adapter);
            showneeddecrp.setAdapter(adapter);
            showkeyenvelop.setAdapter(adapter);
        }else {
            Log.e(TAG,"无法读取文件列表");
        }


        //button事件申请
        Button button1 = findViewById(R.id.uploadown);
        button1.setOnClickListener(this);

        Button button2 = findViewById(R.id.personal);
        button2.setOnClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    //抽象接口的内部方法实现
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.uploadown:
                final Intent intent1 = new Intent(Servicelogic.this, Servicelogic.class);
                //从一个界面跳到另一个界面
                startActivity(intent1);
                break;
            case R.id.personal:
                final Intent intent2 = new Intent(Servicelogic.this, UserRegister.class);
                //启动活动（参数）
                startActivity(intent2);
                break;
        }
    }

    //获得会话密钥
    @SuppressLint("SetTextI18n")
    public void getsessionkey(View view) {
        //获取显示生成结果的文本框
        EditText showsekey=findViewById(R.id.sessionkeyval);
        //保存文件生成的会话密钥
        String safesessionfile="sessionkey.txt";
        FileOutputStream fossessionfile=null;
        try {
            //生成对称密钥并保存
            fossessionfile=openFileOutput(safesessionfile,Context.MODE_PRIVATE);
            String getsesskey= CreateKey.generateKey(fossessionfile);
            if(getsesskey.equals("success")){
                //保存成功就显示出来
                showsekey.setText("会话密钥生成成功，存储路径为："+safesessionfile);
                Log.i(TAG,"会话密钥生成成功");
            }else {
                showsekey.setText("生成失败");
                Log.e(TAG,"会话密钥生成失败");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        //刷新一下列表
        Spinner showencrpfile=findViewById(R.id.choessfile);
        Spinner showsendfile=findViewById(R.id.sendfile);
        Spinner showneeddecrp=findViewById(R.id.showneeddecrp);
        // 获取当前所有文件
        File file = new File(getFilesDir().getPath());
        String[] fileList = file.list();
        // 申请一个列表用于保存添加的item
        if(fileList != null){
            ArrayList<String> items = new ArrayList<>(Arrays.asList(fileList));
            // 将item添加到spinner组件中
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            showencrpfile.setAdapter(adapter);
            showsendfile.setAdapter(adapter);
            showneeddecrp.setAdapter(adapter);
        }else {
            Log.e(TAG,"无法读取文件列表");
        }

    }
    //实现密文文件和密钥信封的生成
    @SuppressLint("SetTextI18n")
    public void encryptfile1(View view) throws IOException {
        //获取当前时间
        LocalDateTime currentDateTime = null;
        String formattedDateTime=null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            currentDateTime = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            formattedDateTime = currentDateTime.format(formatter);
        }
        //定义对称密钥所需的密钥和填充
        byte[] key=new byte[16];
        byte[] iv=new byte[BLOCK_SIZE];
        byte[] rowkey=new byte[32];
        String sessionkeyfile="sessionkey.txt"; //会话密钥存储的位置
        FileInputStream fissesionfile=null;
        //获取当前选中的文件（待加密的文件）
        Spinner chooseencrpfile=findViewById(R.id.choessfile);
        String whichencrp=chooseencrpfile.getSelectedItem().toString();
        FileInputStream fisencrypfile=openFileInput(whichencrp);
        // 分离出文件类型记录，以记录原本发送文件的类型
        String[] filetype=whichencrp.split("\\.");
        //加密文件的存放位置
        String desencrypfile="encryptdata"+formattedDateTime+".txt";
        FileOutputStream fosdesfile=null;
        //密钥信封存储的位置（密钥信封一般不变）
        SharedPreferences sp1=getSharedPreferences("userid",MODE_PRIVATE);
        String ownid=sp1.getString("id","error");
        String keyenvelop="keyEnvelop"+ownid+".txt"; //加上自己的id作为标记
        //连接安全多方进行加密  这个改成罗振宇的ip和端口
        String ip="10.25.2.62";
        int port=8001;
        // 获取对方的id作为标识进行加密  自己测就改成自己的也就是使用ownid
        SharedPreferences sp=getSharedPreferences("getother",MODE_PRIVATE);
        String ids=sp.getString("otherid","error");
        //1.先读取会话密钥和共享的文件
        try {
            //读取出对称密钥
            fissesionfile=openFileInput(sessionkeyfile);
            rowkey=FileReadload.readFilebyte(fissesionfile);
            //分离出key和iv
            System.arraycopy(rowkey, 0, key, 0, 16);
            System.arraycopy(rowkey, 16, iv, 0, 16);
            //2.使用会话密钥加密文件
            fosdesfile=openFileOutput(desencrypfile, Context.MODE_PRIVATE);
            //保存密文数据
            String result= EncAndDec.encrypt(fisencrypfile, fosdesfile,key,iv);
            if(result.equals("success")){
                Toast.makeText(getApplicationContext(),"加密成功",Toast.LENGTH_LONG).show();
                Log.i(TAG,"加密成功，位置为"+desencrypfile);
            }else {
                Log.e(TAG,"加密失败");
                Toast.makeText(getApplicationContext(),"加密失败",Toast.LENGTH_LONG).show();
                return;
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        //3.使用安全多方进行加密
        //密文数据存储位置
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            FileOutputStream foskeylop=openFileOutput(keyenvelop,Context.MODE_PRIVATE);
            Log.i(TAG,"-----加密线程启动-----");
            byte[] finalRowkey = rowkey;
            new Thread(() -> {
                try {
                    //连接安全多方计算进行加密
                    String results=DataInteraction.encrpysession(ownid, finalRowkey,foskeylop,ip,port);
                    if(results.equals("success")){
                        Log.i(TAG2,"密文文件加密成功，保存位置为："+keyenvelop);
                    }else {
                        Log.e(TAG2,"密文文件加密失败");
                    }
                }catch (Exception e){
                    Log.e(TAG2,"连接失败");
                    e.printStackTrace();
                }
            }).start();
        }
        //4.记录加密文件的原始类型
        //将想要传递的数据存放在SharedPreferences中进行持久化存储
        //申请变量，获取的容器名字为sendfile
        final SharedPreferences sp2=getSharedPreferences("sendfile",MODE_PRIVATE);
        final SharedPreferences.Editor editor=sp2.edit();
        editor.putString(desencrypfile.split("\\.")[0],filetype[1]);  //存入数据 加密文件名和被加密文件的原始类型
        editor.putString(keyenvelop.split("\\.")[0],"txt");  //存入数据 密钥信封以.txt形式保存(原始的会话密钥以.txt保存)
        editor.apply();  //提交数据

        Spinner showsendfile=findViewById(R.id.sendfile);
        Spinner showneeddecrp=findViewById(R.id.showneeddecrp);
        // 获取当前所有文件
        File file = new File(getFilesDir().getPath());
        String[] fileList = file.list();
        // 申请一个列表用于保存添加的item
        if(fileList != null){
            ArrayList<String> items = new ArrayList<>(Arrays.asList(fileList));
            // 将item添加到spinner组件中
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            chooseencrpfile.setAdapter(adapter);
            showsendfile.setAdapter(adapter);
            showneeddecrp.setAdapter(adapter);
        }else {
            Log.e(TAG,"无法读取文件列表");
        }
    }
    //实现上传文件
    public void uploadfile(View view) throws FileNotFoundException {
        Spinner choosefile=findViewById(R.id.sendfile);
        //获取当前选中的文件
        String whichupload=choosefile.getSelectedItem().toString();
        SharedPreferences sp2=getSharedPreferences("sendfile",MODE_PRIVATE);
        // 获得原始文件的类型
        String filetype=sp2.getString(whichupload.split("\\.")[0],"error");
        FileInputStream fisencrypfile=openFileInput(whichupload);
        //获得当前可供连接的ip地址  如果没在服务器上就手动修改确定ip和端口
//        SharedPreferences sp1=getSharedPreferences("getip",MODE_PRIVATE);
//        String ip=sp1.getString("whichIP","error"); //提取出当前ip
        String ip="10.25.3.182";
        //协议的端口
        int port_protocol=8881;
        //数据的端口
        int port_data=8882;
        Log.i(TAG,"-----发送线程启动-----");
        // 获取发送方的id地址
        SharedPreferences sp=getSharedPreferences("getother",MODE_PRIVATE);
        String id2=sp.getString("otherid","error");
        // 自己的id
        SharedPreferences sp3=getSharedPreferences("userid",MODE_PRIVATE);
        String id1=sp3.getString("id","error");
        //启动线程发送指定的文件
        new Thread(() -> {
            try {
                String fileinfo=FileReadload.readFileinfo(fisencrypfile,whichupload,filetype,id1,id1);
                if (fileinfo!=null){
                    Log.i(TAG2,"读取文件头信息为："+fileinfo);
                    //发送协议  up#filename#size
                    String result_protocol=DataInteraction.Sendprotocol(fileinfo,ip,port_protocol);
                    if(Objects.equals(result_protocol, "ok")){
                        Log.i(TAG2,"发送协议成功！！");
                        String result_data=DataInteraction.SendData(fisencrypfile,ip,port_data);
                        if(Objects.equals(result_data, "ok")){
                            Log.i(TAG2,"发送文件成功！！");
                        }else {
                            Log.i(TAG2,"发送文件失败！！");
                        }
                    }else {
                        Log.i(TAG2,"发送协议失败！！");
                    }
                }else {
                    Log.i(TAG2,"读取文件失败！！");
                }
            }catch (Exception e){
                Log.e(TAG2,"密文数据连接失败");
                e.printStackTrace();
            }
        }).start();
       Toast.makeText(getApplicationContext(),"发送成功",Toast.LENGTH_LONG).show();
    }

    //实现从远程服务器获取密文文件
    @SuppressLint("SetTextI18n")
    public void downloadfile(View view) throws FileNotFoundException {
        //1.预准备
        Spinner getfiles=findViewById(R.id.showuserfiles);
        String whichfile=getfiles.getSelectedItem().toString(); //这个只有文件名字
        //从key-value中取出datahash和filetype
        SharedPreferences sp=getSharedPreferences("filedatahash",MODE_PRIVATE);
        String filedetail=sp.getString(whichfile,"error"); //提取出文件的类型和datahash
        String[] filedata=filedetail.split("#"); //分离出文件的类型和datahash
        byte[] fileinfo="44be780d009ada30e0ede4c2f50048f2fbbc1f26133aa8bc6146a6c677830ac4".getBytes();  //得到datahash
        String filetype=filedata[0];
        filetype="pdf";
        //2.设置ip地址和端口
        //查看当前可供连接的ip地址
        SharedPreferences sp1=getSharedPreferences("getip",MODE_PRIVATE);
        //String ip=sp1.getString("whichIP","error"); //提取出当前ip
        String ip="10.25.3.182";
        int port=8881;
        // 申请需要下载的文件名字
        String getfile=whichfile+"D."+filetype; //当前选中的文件+类型
        //3.接收文件
        Log.i(TAG,"-----接受文件线程启动-----");
        FileOutputStream fossavefile=openFileOutput(getfile,Context.MODE_PRIVATE);
        new Thread(() -> {
            try {
                //下载文件
                String resultgetdata=DataInteraction.GetData(fossavefile, fileinfo,ip,port);
                if(Objects.equals(resultgetdata, "success")){
                    Log.i(TAG3,"文件接收成功，存放位置为："+getfile);
                }
            }catch (Exception e){
                Log.e(TAG3,"密文数据连接失败");
                e.printStackTrace();
            }
        }
        ).start();
        //休眠1秒
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        //下载之后刷下一下解密的spinner
        Spinner decrpyfile=findViewById(R.id.showneeddecrp);
        Spinner showkeyenvelop=findViewById(R.id.showkeyenvelop);
        // 获取当前所有文件
        File file = new File(getFilesDir().getPath());
        String[] fileList = file.list();
        // 申请一个列表用于保存添加的item
        if(fileList != null){
            ArrayList<String> items = new ArrayList<>(Arrays.asList(fileList));
            // 将item添加到spinner组件中
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            decrpyfile.setAdapter(adapter);
            showkeyenvelop.setAdapter(adapter);
        }else {
            Log.e(TAG,"无法读取文件列表");
        }
        Toast.makeText(getApplicationContext(),"文件下载成功！！！",Toast.LENGTH_LONG).show();

    }
    //实现解密密文文件
    @SuppressLint("SetTextI18n")
    public void decryptfile(View view) throws FileNotFoundException {
        //获取当前时间
        String formattedDateTime=null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            LocalDateTime currentDateTime = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            formattedDateTime = currentDateTime.format(formatter);
        }
        //获取当前选中的文件密文文件
        Spinner getfiles=findViewById(R.id.showneeddecrp);
        String whichfile=getfiles.getSelectedItem().toString(); //这个包括下载的文件类型和文件名
        FileInputStream fisencrpyfile = null;
        //解密之后的数据存储位置
        String decryptdatafile = "downlodefile"+formattedDateTime+whichfile.split("\\.")[1]; //生成文件的保存路径
        FileOutputStream fosdecryptfile = null;
        String dekeyenvelopfile = "dekeyenvelop.txt"; //解密之后的会话密钥
        //定义对称密钥所需的密钥和填充
        byte[] key=new byte[16];
        byte[] iv=new byte[BLOCK_SIZE];
        byte[] rowkey=new byte[32];
        //解密密文数据
        try {
            //读取出对称密钥
            FileInputStream fisgetsession = openFileInput(dekeyenvelopfile);
            rowkey=FileReadload.readFilebyte(fisgetsession);
            //分离出key和iv
            System.arraycopy(rowkey, 0, key, 0, 16);
            System.arraycopy(rowkey, 16, iv, 0, 16);
            //使用会话密钥解密密文数据
            fisencrpyfile = openFileInput(whichfile);
            fosdecryptfile = openFileOutput(decryptdatafile, Context.MODE_PRIVATE);
            String decryptdata = EncAndDec.decrypt(fisencrpyfile, fosdecryptfile, key,iv);
            //判断是否解密成功
            if (Objects.equals(decryptdata, "success")) {
                Log.i(TAG,"解密成功！！");
            } else {
                Log.e(TAG,"解密失败！！");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Toast.makeText(getApplicationContext(),"解密成功！！！",Toast.LENGTH_LONG).show();
    }
    //获取文件的元数据
    @SuppressLint("SetTextI18n")
    public void getfileinfo(View view) {
        //获得选中的哪个文件
        Spinner getfiles=findViewById(R.id.showuserfiles);
        String whichfile=getfiles.getSelectedItem().toString(); //这个只有文件名称
        //将获得的文件显示到下方的点击下载旁边
        EditText showfile=findViewById(R.id.showresult);
        showfile.setText(whichfile);
        Toast.makeText(getApplicationContext(),"获取元数据成功！！！",Toast.LENGTH_LONG).show();
    }
    //连接代理脚本以查询区块链上的属于本机的文件
    public void viewownfile(View view) {
        //脚本的ip和port或者管理节点的ip 这个改成李合计的ip地址和端口
        String ip="10.25.9.11";
        int port=9000;
        //获得需要显示的spinner
        Spinner showneeddecrp=findViewById(R.id.showuserfiles);
        // 获取当前的id和权限
        SharedPreferences sp=getSharedPreferences("userid",MODE_PRIVATE);
        String ids=sp.getString("id","error");
        // 向终端管理节点获得属于自己的文件的datahash
        String get_file_hash="rectometa0000000"+ids;
        AtomicReference<String> get_result = new AtomicReference<>();
        new Thread(() -> {
            try {
                //获得datahash
                Log.i(TAG2,"启动！！！！");
                get_result.set(DataInteraction.InteractingManagement1(get_file_hash, ip, port));
                if (!Objects.equals(get_result.get(), "error")){
                    Log.i(TAG2,"获得成功");
                }else {
                    Log.e(TAG2,"获取失败！！");
                }
            }catch (Exception e){
                Log.e(TAG2,"连接失败");
                e.printStackTrace();
            }
        }).start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        // 解析获得的datahash格式为：文件名#格式#datahash
        String[] getdatahash=get_result.get().split("#");
        //分离出文件名、格式和datahash
        String[] filename=new String[getdatahash.length/3]; // 保存文件名字
        String[] filedetaile=new String[getdatahash.length/3]; //保存文件类型和datahash
        int i=0;
        int j=0;
        while (i<getdatahash.length){
            filename[j]=getdatahash[i];
            filedetaile[j]=getdatahash[i+1]+"#"+getdatahash[i+2]; //将文件类型和datahash拼接
            i+=3;
            j+=1;
        }
        //将想要传递的数据存放在SharedPreferences中进行持久化存储
        //申请变量，获取的容器名字为filedatahash
        final SharedPreferences sp1=getSharedPreferences("filedatahash",MODE_PRIVATE);
        final SharedPreferences.Editor editor=sp1.edit();
        editor.clear(); //清除一下
        for (int k=0;k<filename.length;k++){
            editor.putString(filename[k],filedetaile[k]);  //将文件名和datahash以key-value形式保存
            editor.apply();  //提交数据
        }
        //在前端展示文件的名字
        // 申请一个列表用于保存添加的item
        ArrayList<String> items = new ArrayList<>(Arrays.asList(filename));
        // 将item添加到spinner组件中
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        showneeddecrp.setAdapter(adapter);
        Toast.makeText(getApplicationContext(),"查询文件成功",Toast.LENGTH_LONG).show();

    }
    //调用安全多方解密密钥信封
    public void decryptkeyenvlop(View view) throws FileNotFoundException {
        //读取已经下载好的密钥信封
        Spinner getkeyenvelop=findViewById(R.id.showkeyenvelop);
        String envelopfile = getkeyenvelop.getSelectedItem().toString();
        //解密之后的数据存储位置
        String dekeyenvelopfile = "dekeyenvelop.txt";
        //连接安全多方进行加密  这个也是罗震宇的ip和端口
        String ip = "10.25.2.62";
        int port = 8001;
        //先解密会话密钥==解密密钥信封
        FileInputStream fisenvlLopfile = openFileInput(envelopfile);
        FileOutputStream fissessionkey = openFileOutput(dekeyenvelopfile, Context.MODE_PRIVATE);
        new Thread(() -> {
            try {
                byte[] getsessionkey = DataInteraction.decrpysession(fisenvlLopfile, ip, port);
                if (getsessionkey!=null) {
                    Log.i(TAG3, "密钥信封解密成功！！！");
                    //将密钥保存起来
                    boolean sessionresult = FileReadload.safesessionkey(fissessionkey, getsessionkey);
                    if (sessionresult) {
                        Log.i(TAG3, "会话密钥保存成功！！！保存位置为：" + dekeyenvelopfile);
                    } else {
                        Log.e(TAG3, "会话密钥保存失败！！！");
                    }
                } else {
                    Log.e(TAG3, "密钥信封解密失败！！！");
                }
            } catch (Exception e) {
                Log.e(TAG3, "密钥信封解密失败！！！");
                e.printStackTrace();
            }
        }
        ).start();
        //休眠0.2秒,保证线程执行结束
        try {
            Thread.sleep(500);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        Toast.makeText(getApplicationContext(),"解密密钥信封成功",Toast.LENGTH_LONG).show();
    }
}