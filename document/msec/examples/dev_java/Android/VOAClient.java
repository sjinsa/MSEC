import MainLogic.VOAJavaMainLogic;
import org.msec.rpc.SrpcProxy;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by yuyang on 2016/8/19.
 */
public class VOAClient {
    public static void main(String[] args) {
        SrpcProxy proxy = new SrpcProxy();
        proxy.setCaller("android_1.0");//���������ݣ�������־չ��
        proxy.setMethod("MainLogic.MainLogicService.getTitles1");//���õ�RPC����������Э�����package_name.service_name.rpc_name
        long seq = (long)(1000000 * Math.random());  //����������滻Seq���ɷ���
        proxy.setSequence(seq);//�����Ψһ��ʶ������У��Ӧ�������ֹ����

        VOAJavaMainLogic.GetTitlesRequest.Builder requestBuilder = VOAJavaMainLogic.GetTitlesRequest.newBuilder();
        requestBuilder.setType("special");//������Ĳ�����ʼ��
        VOAJavaMainLogic.GetTitlesRequest request = requestBuilder.build();
        byte[] sendBytes = proxy.serialize(request);	//����������л�Ϊ�ֽ�������ͨ�����緢�͡�ע�⣺���л���Ľ��������SRPC���ڲ�����ͷ
        VOAJavaMainLogic.GetTitlesResponse response;    //�ذ��ṹ��

        //����ͨ�ŵĻ�������
        Socket socket = new Socket();
        String ip = ; // ������IP
        int port = 7963;

        try {
            socket.setSoTimeout(20000);//20�볬ʱ
            socket.connect(new InetSocketAddress(ip, port), 2000);  //���ӷ���

            //����srpc����
            socket.getOutputStream().write(sendBytes);

            //��ʽͨ������£�������Ӧ�����ݣ�ֱ�����һ��������Ӧ����
            byte[] buf = new byte[102400];
            int offset = 0;
            int result = 0;
            while(true)
            {
                int len = socket.getInputStream().read(buf, offset, buf.length-offset);
                if (len <= 0) {
                    throw new Exception("recv package failed");
                }
                offset += len;
                result = proxy.checkPackage(buf, offset);   //��鱨��������
                if (result < 0) {//������                {
                    throw new Exception("check package failed");
                }
                else if (result > 0) //�Ѿ��յ���һ�������ı��ģ�����Ϊresult
                    break;
            }
            response = (VOAJavaMainLogic.GetTitlesResponse)proxy.deserialize(buf, result, VOAJavaMainLogic.GetTitlesResponse.getDefaultInstance());	//��������
            if(response == null) {
                throw new Exception(String.format("Deserialize error: [%d]%s", proxy.getErrno(), proxy.getErrmsg()));
            }

            if( proxy.getSequence() != seq) {//sequenceһ����
                throw new Exception("Sequence mismatch");
            }

            //ҵ�������߼�������
            for(int i = 0; i < response.getTitlesCount(); i++) {
                System.out.println("Resp title: "+ response.getTitles(i));
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
   }
}