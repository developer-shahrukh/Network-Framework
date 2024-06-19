package com.thinking.machines.nframework.client;
import com.thinking.machines.nframework.common.exceptions.*;
import com.thinking.machines.nframework.common.*;
import java.nio.charset.*;
import java.io.*;
import java.net.*;
public class NFrameworkClient
{
public Object execute(String servicePath,Object ...arguments) throws Throwable
{
try
{
Request request=new Request();
request.setServicePath(servicePath);
request.setArguments(arguments);
String requestJSONString=JSONUtil.toJSON(request);

byte objectBytes[];
objectBytes=requestJSONString.getBytes(StandardCharsets.UTF_8);
int requestLength=objectBytes.length;
byte header[]=new byte[1024];
int x;
int i;
i=1023;
x=requestLength;
while(x>0)
{
header[i]=(byte)(x%10);
x=x/10;
i--;
}
Socket socket=new Socket("localhost",5500);
OutputStream os=socket.getOutputStream();
os.write(header,0,1024); // from which index, how many 
os.flush();
InputStream is=socket.getInputStream();
byte ack[]=new byte[1];
int bytesReadCount;
while(true)
{
bytesReadCount=is.read(ack);
if(bytesReadCount==-1) continue;
break;
}
int bytesToSend=requestLength;
int chunkSize=1024;
int j=0;
while(j<bytesToSend)
{
if((bytesToSend-j)<chunkSize) chunkSize=bytesToSend-j;
os.write(objectBytes,j,chunkSize);
os.flush();
j=j+chunkSize;
}
int bytesToReceive=1024;
byte tmp[]=new byte[1024];
int k;
i=0;
j=0;
while(j<bytesToReceive)
{
bytesReadCount=is.read(tmp);
if(bytesReadCount==-1) continue;
for(k=0;k<bytesReadCount;k++)
{
header[i]=tmp[k];
i++;
}
j=j+bytesReadCount;
}
int responseLength=0;
i=1;
j=1023;
while(j>=0)
{
responseLength=responseLength+(header[j]*i);
i=i*10;
j--;
}
ack[0]=1;
os.write(ack,0,1);
os.flush();
byte response[]=new byte[responseLength];
bytesToReceive=responseLength;
i=0;
j=0;
while(j<bytesToReceive)
{
bytesReadCount=is.read(tmp);
if(bytesReadCount==-1) continue;
for(k=0;k<bytesReadCount;k++)
{
response[i]=tmp[k];
i++;
}
j=j+bytesReadCount;
}
ack[0]=1;
os.write(ack);
os.flush();
socket.close();
String responseJSONString=new String(response,StandardCharsets.UTF_8);
Response responseObject=JSONUtil.fromJSON(responseJSONString,Response.class);
if(responseObject.getSuccess())
{
return responseObject.getResult();
}
else
{
throw responseObject.getException();
}
}catch(IOException ioException)
{
System.out.println(ioException);
System.out.println("*************IOException in client side***************");
}
return null;
}
}