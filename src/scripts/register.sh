resource=:8080
token=$(http POST :8080/user/register username=a@a.com password=a123456 | jq ".data.token" | sed "s/\"//g")
echo token: "$token"
http POST :8080/post/new text="我能发帖木？" Authorization:$token