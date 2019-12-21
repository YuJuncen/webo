set token (http POST :8080/user/register username==XiaoMing password==a123456 | jq ".data.token")
http POST :8080/webo/new message="我能发帖木？"
