#include "stdio.h"
#include "stdlib.h"
#include "pthread.h"
#include "sys/time.h"
#include "sys/socket.h"
#include "sys/types.h"
#include "unistd.h"
#include "arpa/inet.h"
#include "string.h"

#define MAX_C 128
#define S_PORT 3333
#define S_IP "0.0.0.0"

typedef struct sockaddr SADDR;
typedef struct sockaddr_in SADDR_IN;
typedef socklen_t SOCK_SIZE;
typedef struct{
	short used;
	int* socket;
	char ip[16];
	char name[256];
} Client;

void* Accept(void*);
void Initialize(void);
short Get(void);
void* Looper(void*);
short QinBox(const char*);
void Broadcast(const char*, const short, char*);
short Scrutineer(const char* info);
void Trim(char* st, const short len);
void Handler(const char*, Client*);
void Stop(void);
void Backspance(const short);
void Kill(Client*);
short Register(const char*);
short Log(char*);

static Client cq[MAX_C];
static int sl = -1;

int main(void)
{
	Initialize();
	pthread_t th;
	pthread_create(&th, NULL, (void*)Accept, &sl);
	//pthread_detach(th);
	printf("The server started, enter \"q\" to stop it.\n");
	while(1)
	{
		// 休眠200毫秒
		usleep(2e5);
		fprintf(stdout, "@server>>>");
		if(getchar() == 'q'){
			break;
		}
	}
	pthread_cancel(th);
	pthread_join(th, NULL);
	Stop();
}

/*
* 监听客户端连接
*/
void* Accept(void* args)
{
	int* listener = (int*)args;
	pthread_t cth;
	SADDR_IN sl_addr, c_addr;  
	SOCK_SIZE c_addr_size = sizeof(c_addr);
	sl_addr.sin_family = AF_INET;
	sl_addr.sin_port = htons(S_PORT);
	sl_addr.sin_addr.s_addr = inet_addr(S_IP);
   	if((*listener = socket(AF_INET, SOCK_STREAM, 0)) == -1)
      	{
         fprintf(stderr, "failed to create server's socket.\n");
         exit(EXIT_FAILURE);
      	}
	if(bind(*listener, (SADDR*)&sl_addr, sizeof(sl_addr)) == -1)
	{
		fprintf(stderr, "failed to bind.\n");
		exit(EXIT_FAILURE);
	}
	if(listen(*listener, MAX_C) == -1)
	{
		fprintf(stderr, "failed to listen.\n");
		exit(EXIT_FAILURE);
	}
	fprintf(stdout, "waiting for the user to connect on [%s:%d]...\n", inet_ntoa(sl_addr.sin_addr), S_PORT);
	
	while(1)
	{
		short cp = Get();
		if(cp == -1) continue;
		int* ts = calloc(1, sizeof(int)); // 使用动态申请内存的方式方式，Kill时手动释放，归还资源。
		//cq[cp].socket = calloc(1, sizeof(int));
		if((*ts = accept(*listener, (SADDR*)&c_addr, &c_addr_size)) != -1)
		{	
			cq[cp].socket = ts;
			pthread_create(&cth, NULL, (void*)Looper, &cq[cp]);
			pthread_detach(cth);	
			inet_ntop(AF_INET, &c_addr.sin_addr, cq[cp].ip, sizeof(cq[cp].ip));
			//strcpy(cq[cp].ip, inet_ntoa(c_addr.sin_addr));
		}
		sleep(3);
	}
	return (void*)0;
}

/*
* 初始化工作
*/
void Initialize(void)
{
	short i = 0;
	for(i; i < MAX_C; i++)
	{
		cq[i].used = -1;
		cq[i].socket = NULL;
		memset(cq[i].ip, '\0', sizeof(cq[i].ip));
		strcpy(cq[i].name, "who?");
	}
}

/*
* 返回一个空闲的客户端索引
*/
short Get(void)
{
	short i = 0;
	for(i; i < MAX_C; i++)
	{
		if(cq[i].used == 0) continue;
		cq[i].used = 0;
		break;
	}
	if(i == MAX_C){i = -1;}
	return i;
}

/*
* 请求读取者
*/
void* Looper(void* args)
{
	Client* client = (Client*)args;
	char req[1024];
	int flag = 1;
	while(1){
		flag = recv(*(client -> socket), req, sizeof(req), 0);
		if(flag <= 0) {
			//printf("\nclient exit: %s, %s\n", client -> name, client -> ip);
			Kill(client);
			break;
		}
		if(strlen(req) <= 0) continue;
		Handler(strtok(req, "#"), client);
		sleep(1);
	}
	return (void*)0;
}

/*
* 请求处理者
*/
void Handler(const char* req, Client* client)
{
	//printf("\n客户端消息：%s\n", req);
	char temp[1024];
	char resp[1024];
	char log[1024];
	char* order;
	char* content;
	memset(temp, '\0', sizeof(temp));
	memset(resp, '\0', sizeof(resp));
	memset(log, '\0', sizeof(log));
	strcpy(temp, req);
	order = strtok(temp, "~");
	sprintf(log, "客户端请求: [name: %s, ip: %s], %s", client -> name, client -> ip, req);
	Log(log);
	if(strcmp(order, "client:broadcast") == 0)	// 广播请求
	{
		content = strtok(NULL, "#");
		sprintf(resp, "SERVER:BRO~%s:%s:%s#", client -> ip, client -> name, content);
		Broadcast(resp, sizeof(resp), "EVERY_ONE");
		return;
	}
	
	if(strcmp(order, "client:register") == 0)
	{
		content = strtok(NULL, "");
		short sign = Register(content);
		if(sign < 0)
		{
			strcpy(resp, "SERVER:REG_DENY#");
		}else{
			strcpy(resp, "SERVER:REG_SUCCESS#");
		}
		write(*(client -> socket), resp, sizeof(resp));
		return;
	}
	
	if(strcmp(order, "client:login") == 0) // 登录请求
	{
		content = strtok(NULL, "");
		if(Scrutineer(content) == 0)
		{
			char n[1024];
			char* username;
			strcpy(n, content);
			username = strtok(n, ":");
			sprintf(resp, "SERVER:WELCOME#");
			strcpy(client -> name, username);
			write(*(client -> socket), resp, sizeof(resp));
			sprintf(resp, "SERVER:LOGIN~%s:%s#", client -> ip, client -> name);
			Broadcast(resp, sizeof(resp), client -> ip);
		}else{
			sprintf(resp, "SERVER:DENY~end~");
			write(*(client -> socket), resp, sizeof(resp));
		}
		return;
	}
	if(strcmp(order, "client:msg") == 0)	// 消息请求
	{
		char* inbox = strtok(NULL, ":");
		short id = QinBox(inbox);
		sprintf(resp, "SERVER:MSG~%s:%s:%s#", client -> ip, client -> name, strtok(NULL, ""));
		write(*(client -> socket), resp, sizeof(resp));
		if(id != -1)
		{
			write(*(cq[id].socket), resp, sizeof(resp));
		}
		return;
	}
}

/*
* 查询收件箱id
*/
short QinBox(const char* ip)
{
	short i = 0;
	for(i; i < MAX_C; i++)
	{
		if(cq[i].used == -1) continue;
		if(strcmp(cq[i].ip, ip) == 0) break;
	}
	if(i == MAX_C) i = -1;
	return i;
}

/*
* 服务器停止
*/
void Stop(void)
{
	short i = 0;
	close(sl);
	char resp[] = "SERVER:STOPPED#";
	Broadcast(resp, sizeof(resp), "EVERY_ONE");
	for(i; i < MAX_C; i++)
	{
		if(cq[i].used = -1) continue;
		cq[i].used = -1;
		close(*(cq[i].socket));
		free(cq[i].socket);
	}
	fprintf(stdout, "Server stopped, bye~.\n");
}

// 广播
void Broadcast(const char* msg, const short len, char* ip)
{
	short i;
	for(i = 0; i < MAX_C; i++)
	{
		if(cq[i].used != -1 && cq[i].socket != NULL && strcmp(cq[i].ip, ip) != 0)
		{
			write(*(cq[i].socket), msg, len);
		}
	}
}

/*
* 用户信息检查
* 返回：0 成功， -1 失败
*/
short Scrutineer(const char* info)
{
    	short k = -1;
    	FILE* fp;
    	char buffer[1024];
	memset(buffer, '\0', sizeof(buffer));
    	if((fp = fopen("./USER_DATABASE", "rb")) == NULL)
    	{
        	fprintf(stderr, "failed to load database.\n");
        	exit(EXIT_FAILURE);
    	}
    	while (fgets(buffer, sizeof(buffer), fp) != NULL && buffer[0] != '\n')
	{
		Trim(buffer, sizeof(buffer));
		if(strcmp(buffer, info) == 0){
            		k = 0;
            		break;
        	}
    	}
	fclose(fp);
    return k;
}

/*
* 注册
*/
short Register(const char* u_p)
{
    FILE* fp;
    short flag = -1;
    if((fp = fopen("./USER_DATABASE", "a+")) == NULL)
    {
        fprintf(stderr, "failed to open USER_DATABASE");
    }
    flag = fprintf(fp, "%s\n", u_p);
    fflush(fp);
    return flag;
}

/*
* 删除字符传入字符串末尾的换行符
*/
void Trim(char* st, const short len)
{
	short i;
	for(i = len; i > -1; i--)
	{
		if(st[i] == '\n')
		{
			st[i] = '\0';
			break;
		}
	}
}

/*
* 释放已经断开的客户端
*/
void Kill(Client* client)
{	
	// 通知其它客户端	
	char resp[1024];
	sprintf(resp, "SERVER:LOGOUT~%s:%s#", client -> ip, client -> name);
	Broadcast(resp, sizeof(resp), client -> ip);
	// 释放资源
	client -> used = -1;
	strcpy(client -> name, "who?");
	memset(client -> ip, '\0', sizeof(client -> ip));
	close(*(client -> socket));
	free(client -> socket);
	client -> socket = NULL;
}

/*
* 输出指定数量的退格符
*/
void Backspance(const short n)
{
	short i = 0;
	for(i = 0; i < n; i++)
	{
		fprintf(stdout, "\b");
	}
}

/*
* 记录日志
*/
short Log(char* log)
{
    FILE* fp;
    char temp[strlen(log) + 2];
    memset(temp, '\0', sizeof(temp));
    sprintf(temp, "%s\n", log);
    if((fp = fopen("./log.txt", "a+")) == NULL)
    {
        fprintf(stderr, "failed to open log file.");
        exit(EXIT_FAILURE);
    }
    if(fputs(temp, fp) == EOF)
    {
        fprintf(stderr, "failed to write log.");
        exit(EXIT_FAILURE);
    }
    fflush(fp);
    if(fclose(fp) == EOF)
    {
        fprintf(stderr, "failed to close log file.");
        exit(EXIT_FAILURE);
    }
    return 0;
}
