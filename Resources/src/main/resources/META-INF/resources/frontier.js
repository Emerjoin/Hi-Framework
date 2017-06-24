
   var withUploads = false;

    //This frontier has arguments
    if(arguments.length>0){

        for(var index in arguments){

            var argument = arguments[index];
            if(Upload.prototype.isPrototypeOf(argument)){

                withUploads = true;
                break;

            }

        }

    }


    var promisse = new Hi.$frontiers.Promise();


    if($fiis.hasOwnProperty(_$fmut)){

        if(_$si){

            if(_$si_method){

                if(_$abpon){

                    $fiis[_$fmut].abort();

                }else
                    return new Hi.$frontiers.Promisse();


            }else{


                if(JSON.stringify(params)==$fiis[_$fmut].$params){

                    if(_$abpon){

                        $fiis[_$fmut].abort();
                        pretendContinuation();

                    }else
                        return new Hi.$frontiers.Promise();



                }

            }

        }

    }

   var ajaxParams = {

       success : function(data){

           if(data.hasOwnProperty("$invoke")){

               var cmdInvocations = data["$invoke"];
               for(var cmdName in cmdInvocations){

                   var cmdParams = cmdInvocations[cmdName];
                   Hi.$ui.js.commands.run(cmdName,cmdParams);


               }

           }

           if(data.hasOwnProperty("$root")){

               if(typeof __!="undefined"){

                   for(var key in data.$root){

                       __[key] = data.$root[key];

                   }

                   __.$apply();

               }

           }

           promisse._setResult(data.result);

       },

       error : function(jqXml, errText,httpError){

           var errorText = jqXml.responseText;
           var exceptionType = undefined;

           try{

               var responseJSON = JSON.parse(errorText);
               if(responseJSON.hasOwnProperty("type")&&responseJSON.hasOwnProperty("details")){

                   promisse._setException(responseJSON);
                   return;

               }

           }catch(err){



           }


           //Request aborted
           if(errText=="abort"){

               return;

           }else if(errText=="timeout"){

               promisse._setTimedOut();

           }else{

               switch(jqXml.status){

                   case 403: promisse._setForbidden(); break;

                   case 408: promisse._setTimedOut(); break;

                   case 421: promisse._setInterrupted(); break;

                   case 429: promisse._setOverRequest(); break;

                   case   0: promisse._setOffline(); break;

                   default : promisse._setException(500); break;

               }

           }


       },

       complete: function(){

           delete $fiis[_$fmut];


       }

       //,timeout: 0 TODO: Set the timeout according to Maximum expected call duration

   };

   ajaxParams.method = "POST";
   ajaxParams.url = $functionUrl;
   ajaxParams.headers = {csrfToken:App.csrfToken};
   ajaxParams.dataType = "json";
   ajaxParams.cache = false;

   if(!withUploads){

       ajaxParams.data = JSON.stringify(params);

   }else{

       var form = new FormData();

       var nonUploadArgs = {};
       var uploadArgs = {};

       for(var argIndex in arguments){

           var argument = arguments[argIndex];

           //This is an upload argument
           if(Upload.prototype.isPrototypeOf(argument)){

                var uploadName = argument.getName();
                uploadArgs[uploadName] = argument.length();
                nonUploadArgs["arg"+argIndex] ="$$$upload:"+uploadName;

                var files = argument.getFiles();

                for(var i=0;i<files.length;i++)
                    form.append(uploadName+"_file_"+i,files[i]);


           }else{

               if(typeof argument!="function")
                   nonUploadArgs["arg"+argIndex] = argument;


           }

       }

       form.append("$uploads",JSON.stringify(uploadArgs));
       form.append("$args",JSON.stringify(nonUploadArgs));
       ajaxParams.processData = false;
       ajaxParams.contentType = false;
       ajaxParams.data = form;

   }

    var $req = $.ajax(ajaxParams);

    promisse._setRequest($req);

    $req.$params = JSON.stringify(params);

    $fiis[_$fmut] = $req;

    return promisse;