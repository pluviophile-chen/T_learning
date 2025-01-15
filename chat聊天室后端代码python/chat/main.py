# main.py
from fastapi import FastAPI, HTTPException, Depends
from fastapi.security import OAuth2PasswordBearer, OAuth2PasswordRequestForm
from pydantic import BaseModel
from typing import List, Optional
from datetime import datetime, timedelta
import jwt
import databases
import sqlalchemy
from sqlalchemy import Table, Column, Integer, String, MetaData, ForeignKey, DateTime, create_engine
#uvicorn main:app --host 0.0.0.0 --port 8000
# 数据库配置
DATABASE_URL = "sqlite:///./chat.db"
database = databases.Database(DATABASE_URL)
metadata = MetaData()

# 数据模型定义
users = Table(
    "users",
    metadata,
    Column("id", Integer, primary_key=True),
    Column("username", String, unique=True, index=True),
    Column("hashed_password", String),
)

chatrooms = Table(
    "chatrooms",
    metadata,
    Column("id", Integer, primary_key=True),
    Column("name", String),
    Column("creator_id", Integer, ForeignKey("users.id")),
    Column("created_at", DateTime, default=datetime.utcnow),
)

messages = Table(
    "messages",
    metadata,
    Column("id", Integer, primary_key=True),
    Column("chatroom_id", Integer, ForeignKey("chatrooms.id")),
    Column("user_id", Integer, ForeignKey("users.id")),
    Column("content", String),
    Column("created_at", DateTime, default=datetime.utcnow),
)

# 创建数据库引擎
engine = create_engine(DATABASE_URL)
metadata.create_all(engine)

# FastAPI 应用实例
app = FastAPI()

# Pydantic 模型
class UserCreate(BaseModel):
    username: str
    password: str

class User(BaseModel):
    id: int
    username: str

class ChatRoom(BaseModel):
    id: int
    name: str
    creator_id: int
    # 将 created_at 改为可选字段
    created_at: Optional[datetime] = None

    class Config:
        # 允许从 ORM 对象创建模型
        from_attributes = True

class Message(BaseModel):
    id: int
    chatroom_id: int
    user_id: int
    content: str
    created_at: Optional[datetime] = None

    class Config:
        from_attributes = True

class ChatRoomCreate(BaseModel):
    name: str

class MessageCreate(BaseModel):
    content: str

# JWT 配置
SECRET_KEY = "your-secret-key"  # 在实际应用中应该使用环境变量
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = 30

oauth2_scheme = OAuth2PasswordBearer(tokenUrl="token")

# 工具函数
async def get_current_user(token: str = Depends(oauth2_scheme)):
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        user_id: int = payload.get("sub")
        if user_id is None:
            raise HTTPException(status_code=401, detail="Invalid authentication credentials")
    except jwt.JWTError:
        raise HTTPException(status_code=401, detail="Invalid authentication credentials")
    
    query = users.select().where(users.c.id == user_id)
    user = await database.fetch_one(query)
    if user is None:
        raise HTTPException(status_code=401, detail="User not found")
    return user

# 启动事件
@app.on_event("startup")
async def startup():
    await database.connect()

# 关闭事件
@app.on_event("shutdown")
async def shutdown():
    await database.disconnect()

# API 路由
@app.post("/register", response_model=User)
async def register(user: UserCreate):
    # 检查用户名是否已存在
    query = users.select().where(users.c.username == user.username)
    existing_user = await database.fetch_one(query)
    if existing_user:
        raise HTTPException(status_code=400, detail="Username already registered")
    
    # 创建新用户
    hashed_password = user.password  # 实际应用中应该使用密码哈希
    query = users.insert().values(
        username=user.username,
        hashed_password=hashed_password
    )
    user_id = await database.execute(query)
    return {"id": user_id, "username": user.username}

@app.post("/token")
async def login(form_data: OAuth2PasswordRequestForm = Depends()):
    # 验证用户
    query = users.select().where(users.c.username == form_data.username)
    user = await database.fetch_one(query)
    if not user:
        raise HTTPException(status_code=400, detail="Incorrect username or password")
    
    if user.hashed_password != form_data.password:  # 实际应用中应该比较哈希值
        raise HTTPException(status_code=400, detail="Incorrect username or password")
    
    # 创建访问令牌
    access_token_expires = timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    access_token = jwt.encode(
        {"sub": str(user.id), "exp": datetime.utcnow() + access_token_expires},
        SECRET_KEY,
        algorithm=ALGORITHM,
    )
    return {"access_token": access_token, "token_type": "bearer"}

@app.post("/chatrooms", response_model=ChatRoom)
async def create_chatroom(
    chatroom: ChatRoomCreate,
    current_user: User = Depends(get_current_user)
):
    query = chatrooms.insert().values(
        name=chatroom.name,
        creator_id=current_user.id
    )
    chatroom_id = await database.execute(query)
    
    created_chatroom = await database.fetch_one(
        chatrooms.select().where(chatrooms.c.id == chatroom_id)
    )
    return created_chatroom

@app.get("/chatrooms", response_model=List[ChatRoom])
async def get_chatrooms(current_user: User = Depends(get_current_user)):
    query = chatrooms.select()
    return await database.fetch_all(query)

@app.post("/chatrooms/{chatroom_id}/messages", response_model=Message)
async def create_message(
    chatroom_id: int,
    message: MessageCreate,
    current_user: User = Depends(get_current_user)
):
    # 检查聊天室是否存在
    chatroom_query = chatrooms.select().where(chatrooms.c.id == chatroom_id)
    chatroom = await database.fetch_one(chatroom_query)
    if not chatroom:
        raise HTTPException(status_code=404, detail="Chatroom not found")
    
    # 创建消息
    query = messages.insert().values(
        chatroom_id=chatroom_id,
        user_id=current_user.id,
        content=message.content
    )
    message_id = await database.execute(query)
    
    created_message = await database.fetch_one(
        messages.select().where(messages.c.id == message_id)
    )
    return created_message

@app.get("/chatrooms/{chatroom_id}/messages", response_model=List[Message])
async def get_messages(
    chatroom_id: int,
    current_user: User = Depends(get_current_user)
):
    # 检查聊天室是否存在
    chatroom_query = chatrooms.select().where(chatrooms.c.id == chatroom_id)
    chatroom = await database.fetch_one(chatroom_query)
    if not chatroom:
        raise HTTPException(status_code=404, detail="Chatroom not found")
    
    # 获取消息
    query = messages.select().where(messages.c.chatroom_id == chatroom_id)
    return await database.fetch_all(query)

# 获取用户信息的端点
@app.get("/users/me", response_model=User)
async def get_current_user_info(current_user: User = Depends(get_current_user)):
    return current_user

# 删除聊天室
@app.delete("/chatrooms/{chatroom_id}")
async def delete_chatroom(
    chatroom_id: int,
    current_user: User = Depends(get_current_user)
):
    # 检查聊天室是否存在
    chatroom_query = chatrooms.select().where(chatrooms.c.id == chatroom_id)
    chatroom = await database.fetch_one(chatroom_query)
    if not chatroom:
        raise HTTPException(status_code=404, detail="Chatroom not found")
    
    # 检查是否是聊天室创建者
    if chatroom.creator_id != current_user.id:
        raise HTTPException(
            status_code=403, 
            detail="You don't have permission to delete this chatroom"
        )
    
    # 首先删除聊天室中的所有消息
    delete_messages_query = messages.delete().where(
        messages.c.chatroom_id == chatroom_id
    )
    await database.execute(delete_messages_query)
    
    # 删除聊天室
    delete_chatroom_query = chatrooms.delete().where(
        chatrooms.c.id == chatroom_id
    )
    await database.execute(delete_chatroom_query)
    
    return {"message": "Chatroom deleted successfully"}