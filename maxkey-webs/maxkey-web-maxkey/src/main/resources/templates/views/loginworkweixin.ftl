     <script type="text/javascript" src="https://rescdn.qqmail.com/node/ww/wwopenmng/js/sso/wwLogin-1.0.0.js"></script>
     <script type="text/javascript"> 
        $(function(){
           $("#qrcodelogin").on("click",function(){
              $.get("<@base />/logon/oauth20/scanqrcode/workweixin",function(data,status){
                        window.WwLogin({
                            "id" : "div_qrcodelogin",  
                            "appid" : data.clientId,
                            "agentid" : data.agentId,
                            "redirect_uri" :encodeURIComponent(data.redirectUri),
                            "state" : data.state,
                            "href" : "data:text/css;base64,LmltcG93ZXJCb3ggLnFyY29kZSB7d2lkdGg6IDI1MHB4O30NCi5pbXBvd2VyQm94IC50aXRsZSB7ZGlzcGxheTogbm9uZTt9DQouaW1wb3dlckJveCAuaW5mbyB7d2lkdGg6IDI1MHB4O30NCi5zdGF0dXNfaWNvbiB7ZGlzcGxheTpub25lfQ0KLmltcG93ZXJCb3ggLnN0YXR1cyB7dGV4dC1hbGlnbjogY2VudGVyO30=",
                        });
                        $('#div_qrcodelogin').show();
                    });
            });
        });
    </script> 