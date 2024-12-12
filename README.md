# Distributed Systems KUL - project Bulletin Board

## Specifications to run the project
There are two ways to run the project. The simplest way is to start the server by executing the run_server.bat. After that you can start as many clients as you want by executing the run_client.bat.   


In case this doesn't work you can start the Server.jar and Client.jar manually. You need to go to the directory of the project in your cmd and run following commands.  \
To start the server: 
>java -jar --enable-preview Server.jar

To start the client:  
>java -jar --enable-preview Client.jar

These are the same commands that are executed in the .bat-scripts.  


You can also go into the Server (or Client) directory and run the Server.java (or client.java) in the project itself.

When starting a new chat, you need to specify the other user you want to communicate with. When you do this in both clients, the security information of that client (for this chat) will be shown in the cmd. You need to copy the information in the correct client, this is the initialisation step.


## The project
We implemented the proposed Bulletin board described in the paper ‘Privately (and Unlinkably) Exchanging Messages Using a Public Bulletin Board’ [1]. As communication technology we used RMI and we used Java JCA/JCE to implement the crypto protocols. 



## Resources
[1] J.-H. Hoepman, ‘Privately (and Unlinkably) Exchanging Messages Using a Public Bulletin Board’, in Proceedings of the 14th ACM Workshop on Privacy in the Electronic Society, in WPES ’15. New York, NY, USA: Association for Computing Machinery, Oct. 2015, pp. 85–94. doi: 10.1145/2808138.2808142.
