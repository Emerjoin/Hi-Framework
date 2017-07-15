
/**
 * Hi Framework Library
 */



var __ = {};



var Hi = {};


Hi.$template = undefined;
Hi.$view = undefined;


/**
 * Contains directives, services and other kind of objects directly related to angular
 */
Hi.$angular = {};

/**
 * Holds configurations of the Hi application.
 */
Hi.$config = {};


Hi.$config.nav = {};
Hi.$config.nav.changeLocation = true;


/**
 * Hi-Framework Logging
 * @type {boolean}
 */
Hi.$config.debugLogs = false;
var Log = {};
Log.debug = function(content){

    if(Hi.$config.debugLogs)
        console.error(content);

};


/**
 * Configurations related to what the user sees
 */
Hi.$config.ui = {};

/**
 * Configurations related to the business logic and view control layer - angular JS.
 */
Hi.$config.angular = {};

//{{config}}


/**
 * Handles all internal processes related to the user-interface
 */
Hi.$ui = {};

/**
 * Holds processes related to user interface markup processement.
 */
Hi.$ui.html = {};

/**
 * Holds processes related to user interface javascript scoping
 */
Hi.$ui.js = {};


/**
 * Handles user navigation window events
 */
Hi.$nav = {};


/**
 * Handles all other operations not handled on none of the previous packages
 */
Hi.$util = {};

/**
 * Contains logic that allows to test the Hi application
 */
Hi.$test = {};


Hi.$test.fakeFrontierPromise = function(){



};

Hi.$test.MockCall = function(){

    this.success = function(data){

        var promise = new Hi.$frontiers.Promise();
        promise.run = function(){

            promise._setResult(data);
            return promise;

        };

        return promise;

    };


    this.error = function(error){

        var promise = new Hi.$frontiers.Promise();
        promise.run = function(){

            promise._setException(error);

            return promise;

        };

        return promise;
    };

    //TODO: Test other situations: overrequest, timeout, etc

};


Hi.$test.MockTemplate = function(data){

    Hi.template(data);

};


Hi.$test.ViewTestPromise = function(path){

    this.route = Hi.$nav.resolveRoute(path);


    this.receptor = {};
    this.gettable = {};
    this.gettable.html = function(){

        //Get the transformed HTML
        return this.receptor.markup;

    };

    this.gettable.controller = function(){

        //Get the controller of the view
        return this.receptor.scope;

    };

    if(!(this.route.hasOwnProperty("controller")&&this.route.hasOwnProperty("action"))){

        console.warn("No controller and action names detected. Suspending preparation")
        this.gettable = false;

    }

    this.viewHtml = "";


    this.build = function(){

        Hi.$config.nav.changeLocation = false;

        if(!this.gettable)
            return false;

        var vpath = Hi.$nav.getViewPath(this.route.controller,this.route.action);

        Hi.$ui.js.createViewScope(vpath,{},this.viewHtml,false,this.receptor,false,undefined);


        if(!this.receptor.hasOwnProperty("scope")){

            console.warn("No scope returned to receptor");
            return false;

        }



        this.receptor.scope.$embed = function(){

            //TODO: Implement this

        };


        this.gettable.receptor = this.receptor;
        return this.gettable;


    };


    this.withHtml = function(html){

        //Sets the markup to be used when creating the view scope
        this.viewHtml = html;
        return this;


    };


};

Hi.$test.view = function(path){

    return new Hi.$test.ViewTestPromise(path);

};

//Packages definitions end here


/**
 * - - - - - - - - - - - - -
 * ANGULAR
 * - - - - - - - - - - - - -
 */

/**
 * Angular directives provided by the framework
 */
Hi.$angular.directives = {};

/**
 * This is an attribute restricted directive.
 * Loads an image asynchronously and displays a loader during the process. This directive should be used along with and <img> element.
 * The <img> element should have two attributes: asrc and aload. The aload attribute is the ajax loader and the asrc is the image
 * to be loaded.
 */
Hi.$angular.directives.aload = function(){

    return {

        restrict : 'A',

        link : function(scope,element,attrs){

            var i = new Image();

            var img_source = false;
            var loader = false;
            var loadError = false;




            if(attrs.hasOwnProperty('asrc')){

                img_source = attrs.asrc;

            }else{

                return;

            }


            var doMagic = function(){

                loader = attrs.aload;
                loadError = loader;

                //Error image
                if(attrs.hasOwnProperty("err")){

                    loadError = attrs.err;

                }


                //Mostra o loader
                $(element).attr('src',loader);

                //Carrega a imagem de forma asincrona
                var i = new Image();

                i.onload = function () {

                    //console.log('loaded image '+img_source);
                    $(element).attr('src',img_source);

                };
                i.onerror = function(){

                    $(element).attr('src',loadError);
                    //console.error('error loading image : '+img_source);

                };

                i.src = img_source;


            };

            doMagic();

            attrs.$observe("asrc",function(new_value){


                if(typeof new_value!="undefined"){


                    img_source = attrs.asrc;
                    doMagic();

                }

            });



        }


    };

};


/**
 * This is an element restricted directive.
 * The directive allows processing a route a displaying its content
 * within the active route's view.
 *
 * This directive uses 4 attribute to configure the embedded content
 * a) name - the name of the controller object to be created on the active View's scope
 *
 * b) view - static view embedding
 *
 * c) onLoad - an angular expression that resolves to a function to be invoked while the embedded view is loaded
 *
 * d) onRead - an angular expression that resolves to a function to be invoked when the embedded view is fully loaded
 *
 * Scoping: the scope created for the embedded view is child of the active view's scope.
 *
 */
Hi.$angular.directives.view = function(){

    //TODO: Prevent self embedded

    return {

        restrict:'E',
        scope:false,
        link:function($scope,element, attrs){

            var url = false;

            if(!attrs.hasOwnProperty('name'))
                throw new Error("No name attribute defined on view element");

            if(attrs.hasOwnProperty('embed'))
                url = attrs.embed;

            var onLoadAttr = "onbusy";
            var onLoad = false;
            if(attrs.hasOwnProperty(onLoadAttr))
                onLoad = attrs[onLoadAttr];

            var onReadAttr = "onready";
            var onReady = false;
            if(attrs.hasOwnProperty(onReadAttr))
                onReady = attrs[onReadAttr];

            var onErrorAttr = "onfail";
            var onError = false;
            if(attrs.hasOwnProperty(onErrorAttr))
                onError = attrs[onErrorAttr];


            var $doEmbed = function(url, sucess,err,load){

                if(typeof url !="string" || url.length<1)
                    throw new Error("URL is now valid");

                if(typeof load == "function")
                    load.call($scope);

                var embedOptions =  {};
                embedOptions.onError = function(){

                    if(typeof err =="function")
                        err.call($scope);

                };

                try {

                    Hi.$nav.navigateTo(url, false, true, function (receptor) {

                        receptor.scope.$element = receptor.element;
                        receptor.scope.$embed = $doEmbed;


                        $scope[attrs.name] = receptor.scope;
                        $scope.$applyAsync(function () {

                            $(element).empty();
                            $(element).append(receptor.element);

                            if(typeof sucess=="function")
                                sucess.call();


                        });

                    }, $scope, embedOptions);

                }catch(error){

                    if(typeof err =="function")
                        err.call($scope);

                }

            };

            if(!url) $scope[attrs.name] = {$embed: $doEmbed};
            else $doEmbed(url,function(){

                if(typeof onReady == "string")
                    $scope.$eval(onReady);

            },function(){

                if(typeof onError == "string")
                    $scope.$eval(onError);


            },function(){

                if(typeof onLoad == "string")
                    $scope.$eval(onLoad);
            });

        }

    }

};

/**
 * This is an attribute restricted directive
 * This directive requires the presence of the href attribute on the element.
 * It sets up an onClick event that redirects the application
 * to the url expressed in the href attribute.
 */
Hi.$angular.directives.ajaxify = function(){
    return  {

        restrict : 'A',
        link : function($scope,element,attrs){

            $(element).removeAttr('ajaxify');


            if(attrs.href){

                var href = attrs.href;

                var event = 'click';

                $(element).attr('href','javascript:void(0)');

                if(attrs.on){

                    event = attrs.on;
                    $(element).removeAttr('on');

                }

                Hi.$nav.bind($(element),href,event);

            }

        }

    };


};

/**
 * Defines all built-in angular directives in a angular module
 * @param angularModule the angular module where the directives should be defined
 */
Hi.$angular.directivesDefiner = function(angularModule){

    this.angularModule = angularModule;
    this.setModule = function(module){

        this.angularModule = module;

    };

    this.define = function(){

        if(typeof this.angularModule=="object"){

            if(typeof this.angularModule.directive!="function"){

                throw new Error("Angular directives could not defined. No valid angular module supplied");

            }

        }

        for(var directiveName in Hi.$angular.directives){

            var directiveDefinition = Hi.$angular.directives[directiveName];
            this.angularModule.directive(directiveName,directiveDefinition);

        }

    };

};


/**
 * Directive to handle files upload
 */
Hi.$angular.directives.ngUpload =  function($parse) {

    return {

        restrict: 'A',
        scope : false,
        link: function (scope, element, attrs) {


            var name = attrs["ngUpload"];
            var onUploadFunc = undefined;

            if(name.trim().length<1){

                throw new Error("Invalid upload name : "+name);

            }

            if(!attrs.hasOwnProperty("type"))
                throw new Error("The type property is missing on the upload element with name '"+ name+"'");


            if(attrs.type!=="file")
                throw new Error("The upload element with name '"+name+"' must be of type \"file\"");


            if(attrs.hasOwnProperty("onfiles")){

               onUploadFunc = $parse(attrs["onfiles"]);

            }


            $(element).change(function(event){

                var files = event.target.files;


                var upload = new Upload(name,files);
                setToScope(upload);
                fireEvent(upload);

            });

            var setToScope = function(upload){

                scope[name] = upload;


            };


            var fireEvent = function(up){

                if(typeof onUploadFunc!="undefined")
                    onUploadFunc(scope,{upload: up});

            };


        }


    }

};


/**
 * Application's angular module.
 */
Hi.$angular.app = false;

/**
 * Creates the application module and defines all the
 * built-in directives
 */
Hi.$angular.run = function(){

    var modulesInjected = [];
    var appRun = false;


            if(Array.isArray(Hi.$config.angular.modules)){

                modulesInjected = Hi.$config.angular.modules;
                Log.debug("Injecting the following modules to Hi Application : ");
                Log.debug(modulesInjected);

            }else{

                Log.debug("No modules to be injected to Hi Application");

            }

            if(typeof Hi.$config.angular.run=="function"){

                appRun = Hi.$config.angular.run;

            }else{

                Log.debug("No run function for Hi Application");

            }


    if(Array.isArray(modulesInjected)){

        if(modulesInjected.indexOf("ng")==-1)
            modulesInjected.push('ng');

    }else{

        throw new Error("Invalid value set for property Hi.$config.angular.modules");

    }

    if(modulesInjected.length==0)
        modulesInjected.push("ng");


    var angularApp = angular.module('hi', modulesInjected);
    //Hooks-API is present
    if(typeof AppHooks!="undefined")
        AppHooks.fireBeforeRun(angularApp);

    var appModule = angular.module("app",["ng"]);
    if(typeof AppHooks!="undefined")
        AppHooks.fireSetupApp(appModule);


    var directives = new Hi.$angular.directivesDefiner(angularApp);
    directives.define();

    var runapp = function($rootScope,$compile){

        if(typeof sessionStorage!="undefined"){

            //Application is under test
            if(typeof App=="undefined"){

                Hi.$ui.html.cache.on = false;

            }else {

                var oldDeployId = false;
                if (sessionStorage.hasOwnProperty("deploy-id-"+App.base_url))
                    oldDeployId = sessionStorage["deploy-id-"+App.base_url];
                else{

                    Hi.$ui.html.cache.destroy();
                    Hi.i18n.cache.destroy();

                }



                if (oldDeployId) {

                    //Not the same deploy Id
                    if (oldDeployId != App.deployId) {
                        Hi.$ui.html.cache.destroy();
                        Hi.i18n.cache.destroy();
                    }

                }

                sessionStorage["deploy-id-"+App.base_url] = App.deployId;

                if (App.deployMode == "DEVELOPMENT")
                    Hi.$ui.html.cache.on = false;

            }


        }

        Hi.i18n.init();

        Hi.$angular.$injector =  angular.injector(modulesInjected);
        Hi.$angular.$compile = $compile;

        __=$rootScope;
        Hi.$template = $rootScope;
        __.$startedUp = false;

        for(var propName in Hi.$ui.js.root){

            var propValue = Hi.$ui.js.root[propName];
            __[propName] = propValue;

        }


        if(__.hasOwnProperty("$init")){

            if(typeof __.$init=="function"){

                __.$init.call(__);

            }

        }


        Hi.$angular.$injector =  angular.injector(["app"].concat(modulesInjected));


        //App is not under tests
        if(typeof App!="undefined") {

            if (typeof appRun == "function") {

                Hi.$angular.$injector.invoke(appRun);

            }

            if (typeof $startup != "function") {

                throw new Error("$startup function is undefined");

            }

            //TODO: Review this code
            setTimeout(function () {

                $startup();

            }, 5);


        }

    };

    runapp.$inject = ["$rootScope","$compile"];
    angularApp.run(runapp);
    Hi.$angular.app = angularApp;


};


/**
 * - - - - - - - -
 * UI
 * - - - - - - - -
 */

//HTML Cache
Hi.$ui.html.cache = {on:true};

//Initialize the cache
Hi.$ui.html.cache.initialize = function(){
    try {

        if (sessionStorage) {

            if (!sessionStorage['hi_cache_regs']) {
                sessionStorage['hi_cache_regs'] = JSON.stringify(new Array());
            }

        }

    }catch(err){

        return false;

    }

};

Hi.$ui.html.prepareView = function(route_name_or_object){

    var route = false;

    if(typeof route_name_or_object ==='string'){

        route = Hi.$nav.getNamedRoute(route_name_or_object);

    }else{

        route = route_name_or_object;

    }

    if(route){

        var path = Hi.$nav.getTextViewPath(route.controller, route.action);
        var url = Hi.$nav.getURL(route);
        if(Hi.$ui.html.cache.stores(url)){
            return;
        }

        $.get(url,function(response){


            try{

                var JSONResponse = JSON.parse(response);
                var html = JSONResponse.markup;
                Hi.$ui.html.cache.storeView(url,html);


            }catch(err){


            }


        });



    }

};



Hi.$ui.html.cache.getStorageKey = function(){

    return "hi-app-views-"+App.base_url;

};

Hi.$ui.html.cache.updateCache = function(cache){



    if(typeof localStorage!="undefined"&&typeof cache=="object"){

        localStorage[Hi.$ui.html.cache.getStorageKey()] = JSON.stringify(cache);

    }

};

Hi.$ui.html.cache.getCache = function(){

    var theCache = {};

    if(typeof localStorage!="undefined"){

        var key = Hi.$ui.html.cache.getStorageKey();

        if(localStorage.hasOwnProperty(key)){

            try {

                theCache = JSON.parse(localStorage[key]);

            }catch (err){

                Log.debug("There was an error when trying to parse views cache JSON");

            }

        }

    }

    return theCache;

};




//Limpa a cache
Hi.$ui.html.cache.destroy = function(){

    if(typeof localStorage!="undefined"){

        Hi.$ui.html.cache.updateCache({});

    }

};


//Coloca uma view na cache
Hi.$ui.html.cache.storeView = function(path,markup){


    if(!Hi.$ui.html.cache.on){

        return false;

    }


    var toStore = $("<div>");
    toStore.html(markup);
    $(toStore).find(".hi").remove();
    var html = $(toStore).html();

    var cache = Hi.$ui.html.cache.getCache();
    cache[Hi.$ui.html.cache.normalizePath(path)] = html;
    Hi.$ui.html.cache.updateCache(cache);

    Log.debug("Caching view of path <"+path+">");


};


Hi.$ui.html.cache.normalizePath = function(path){

    var qIndex = path.indexOf('?');

    if(qIndex!=-1){

        return path.substr(0,qIndex);

    }

    return path;

};

//Verifica se a cache contem uma determinada view
Hi.$ui.html.cache.stores = function(path){



    if(!Hi.$ui.html.cache.on){

        return false;

    }

    var cache = Hi.$ui.html.cache.getCache();
    return cache.hasOwnProperty(Hi.$ui.html.cache.normalizePath(path));

};

//Obtem uma view da cahe
Hi.$ui.html.cache.fetch = function(path){

    var cache = Hi.$ui.html.cache.getCache();
    return cache[Hi.$ui.html.cache.normalizePath(path)];


};

//Definir o titulo da pagina
Hi.$ui.html.setTitle = function(title){

    $("title").html(title);

};


Hi.$ui.html.getTitle = function(){

    return $("title").html();

};


Hi.$ui.js.controllers = {};
Hi.$ui.js.loadedControllers = [];

Hi.$ui.js.setLoadedController = function(controller, action){

    Hi.$ui.js.loadedControllers.push(controller+"/"+action);

}

Hi.$ui.js.wasControllerLoaded = function(controller,action){

    var url = controller+"/"+action;
    var urlIndex = Hi.$ui.js.loadedControllers.indexOf(url);
    if(urlIndex==-1){

        return false;

    }

    return true;

};





//Controlador do template
Hi.$ui.js.templatesParent={};

Hi.$ui.js.getViewController = function(controllerName, actionName){

    var view_path = Hi.$nav.getTextViewPath(controllerName,actionName);

    if(Hi.$ui.js.controllers.hasOwnProperty(view_path)){

        var controller = Hi.$ui.js.controllers[view_path];
        return controller;

    }else{

        return false;

    }

};

//Regista um closure de uma view
Hi.$ui.js.setViewController= function(controllerName, actionName, controller){
    var view_path = Hi.$nav.getTextViewPath(controllerName,actionName);
    Hi.$ui.js.controllers[view_path]=controller;
};


Hi.$ui.js.createViewScope = function(viewPath,context_variables,markup,embedded,receptor,$embedScope,embedOptions){

    //Get the view controller
    var controller = Hi.$ui.js.getViewController(viewPath.controller,viewPath.action);

    if(typeof controller=="undefined"){

        throw new Error("No controller defined. Cant prepare context");
        return;

    }

    if(typeof controller!="function"){

        throw new Error("Invalid view controller");


    }


    var viewScope = false;

    if(embedded){

        if(typeof $embedScope!="undefined"){

            viewScope = $embedScope.$new(false);

        }else{

            //Create a new scope from $rootScope;
            viewScope = __.$new(false);

        }


    }else{

        if(!__.$startedUp&&__.hasOwnProperty("$startup")){

            if(typeof __.$startup=="function"){

                __.$startedUp = true;
                __.$startup.call(__,{controller:viewPath.controller,action:viewPath.action});

            }

        }

        //Create a new scope from $rootScope;
        viewScope = __.$new(true);


    }



    //Inject the scope to the controller function
    var $injector = Hi.$angular.$injector;
    var injectables = {_:viewScope,__:__,$scope:viewScope,$rootScope:__,template:__};

    var route = viewPath.controller+"/"+viewPath.action;

    //Hooks API is available
    if(typeof AppHooks!="undefined")
        AppHooks.fireBeforeView(route,viewScope,injectables);

    $injector.invoke(controller,false,injectables);

    //Apply context variables
    Hi.$ui.js.setScopeProps(viewScope,context_variables);


    //PreLoad on view

    if(viewScope.hasOwnProperty('$preLoad')){


        var newMarkup = viewScope.$preLoad.call(viewScope,markup);
        if(typeof newMarkup!="undefined"){

            markup = newMarkup;

        }


    }

    //PreLoad on template
    if(__.hasOwnProperty("$onPreLoad")){

        if(typeof __.$onPreLoad == "function") {

            var newMarkup = __.$onPreLoad.call(__,viewScope.$route, viewScope, markup);
            if (typeof newMarkup != "undefined") {

                markup = newMarkup;

            }

        }

    }



    var compileFn = Hi.$angular.$compile(markup);
    var compiledElement = compileFn(viewScope);

    var changePath = function(){

        var path  = Hi.$nav.getURL(context_variables.$route);
        var routeIndex = Hi.$nav.setActivePath(path);
        Hi.$nav.setLocation(path,JSON.stringify({index:routeIndex}));

    };

    if(receptor && embedded){

        //NOTE: if embedded is true, then receptor will also be true

        receptor.element =compiledElement;
        receptor.scope = viewScope;
        receptor.markup = markup;



        if(typeof viewScope.$postLoad!="undefined"){

            viewScope.$postLoad.call(viewScope);

        }

        //Change the Path

        var setPageLocation = Hi.$config.nav.changeLocation;

        if(embedded)
            setPageLocation = false;


        if(setPageLocation){

           changePath();

        }


        return receptor;

    }

    var closePromise = {};
    closePromise.proceed = function(){

        //Tell the the template that the view was closed
        if(__.hasOwnProperty("$onClose")&&__.hasOwnProperty("$activeView")){
            if(typeof __.$onClose=="function"){
                __.$onClose.call(__,__.$activeView.$route);
            }
        }

        var isRedirect = __.hasOwnProperty("$activeView");


        //Tell the template that there is another active view
        __.$activeView = viewScope;
        Hi.$view = viewScope;


        $("#view_content").html("");
        $("#view_content").append(compiledElement);


        //Change the path
        var setPageLocation = Hi.$config.nav.changeLocation;

        if(setPageLocation&&isRedirect){

            changePath();

        }

        viewScope.$apply(function(){


            if(typeof viewScope.$postLoad!="undefined"){

                viewScope.$postLoad.call(viewScope);

            }

            if(__.hasOwnProperty("$onPostLoad")){

                if(typeof __.$onPostLoad=="function"){

                    __.$onPostLoad.call(__,viewScope.$route,viewScope);

                }

            }


            if(__.hasOwnProperty("$onRedirectFinish")&&isRedirect){

                if(typeof __.$onRedirectFinish=="function"){

                    __.$onRedirectFinish.call(__);

                }

            }

        });


    };


    //Close the active view first
    if(__.hasOwnProperty("$activeView")){


        if(typeof __.$activeView=="object"){


            //is close prevent active?
            if(__.$activeView.hasOwnProperty("$preventClose")&&__.$activeView.hasOwnProperty("$close")){


                if(__.$activeView.$preventClose){

                    __.$activeView.$close.call(__.$activeView,closePromise);

                }else{

                    closePromise.proceed();

                }


            }else{

                if(typeof __.$activeView.$close=="function"){

                    //Call the close handler on the view
                    __.$activeView.$close.call(__.$activeView);

                    closePromise.proceed();

                }else{

                    closePromise.proceed();

                }

            }


        }else{

            throw new Error("Invalid active view object");

        }

    }else{

        closePromise.proceed();

    }


};

Hi.$ui.js.commands = {};

Hi.$ui.js.commands.all={};

Hi.$ui.js.commands.set = function(key,callback){

    Hi.$ui.js.commands.all[key]=callback;

};

Hi.$ui.js.commands.set('$reload',function(data){

  if(data.hasOwnProperty("url")){

      document.location.replace(data.url);

  }else{

      document.location.reload();

  }

  return true;

});



//Update the url shown in browser
Hi.$ui.js.commands.set('$url',function(data){

    if(data.hasOwnProperty("value"))
        Hi.$nav.setLocation(data.value,{});

});


Hi.$ui.js.commands.set("$reloadLanguage",function(data){

    Hi.$ui.html.cache.destroy();
    Hi.i18n.cache.destroy();
    document.location.reload();

});

//Redirect the user ajaxically
Hi.$ui.js.commands.set('$redirect',function(data){

    if(data.hasOwnProperty("url"))
        Hi.redirect(data.url);

});

Hi.$ui.js.commands.run = function(key,params){

    if(Hi.$ui.js.commands.all.hasOwnProperty(key)){

        var command = Hi.$ui.js.commands.all[key];
        return command(params);


    }

    return false;

};

Hi.$ui.js.setScopeProps = function(context,context_variables){

    var UIRoot = {};

    if(context_variables.$root){
        UIRoot = context_variables.$root;
    }

    if(context_variables.hasOwnProperty("$dictionary")){

        var dictionary = context_variables["$dictionary"];
        for(var key in dictionary){
            if(dictionary.hasOwnProperty(key))
                Hi.i18n.dictionary[key] = dictionary[key];
        }

        delete context_variables["$dictionary"];
        delete context_variables["$dictionary-no-cache"];
    }

    for(var root_variable_name in UIRoot){
        var root_variable_value = UIRoot[root_variable_name];
        __[root_variable_name]=root_variable_value;
    }

    __['__t']=__t;


    for(var context_variable_name in context_variables){

        var context_variable_value = context_variables[context_variable_name];
        context[context_variable_name]=context_variable_value;
    }



};


Hi.$ui.js.root = {};


/**
 * Register a Web UI Component
 */

Hi.$ui.js.component = function(name, directive){

    Hi.$angular.directives[name] = directive;

};



/**
 * - - - - - - - -
 * UTIL
 * - - - - - - - -
 */

Hi.$util.strip = function(obj){

    var json =angular.toJson(obj);

    return JSON.parse(json);

};

Hi.$util.empty = function(){

    return angular.copy({});

};


Hi.$util.ucfirst = function(str){

    var firstLetter = str.substr(0, 1);
    return firstLetter.toUpperCase() + str.substr(1);

};

Hi.$util.lcfirst = function(str){

    var firstLetter = str.substr(0, 1);
    return firstLetter.toLowerCase() + str.substr(1);

};


Hi.$util.URLToJson = function(params){

    var pairs = params.split("&");
    var result = {};

    for(var pairIndex in pairs){

        var pair = pairs[pairIndex];
        if(typeof pair!="string")
            continue;

        var eqIndex = pair.indexOf("=");
        var paramName = pair.substring(0,eqIndex);
        var paramValue = pair.substring(eqIndex+1,pair.length);
        result[paramName] = paramValue;

    }

    return result;

};

Hi.$util.encodeGetParams = function(params){

    var params_uri = "";

    var index = 0;
    for(var param_name in params){

        var param_value = params[param_name];

        if(index!==0){

            params_uri = params_uri+'&';

        }

        params_uri = params_uri+param_name+'='+param_value;

        index++;

    }

    return encodeURI(params_uri);


};

Hi.$util.getKidProperties = function(name,attrs){




    var kids = [];


    for(var propertyIndex in attrs){

        if(propertyIndex.indexOf(name)>-1){

            var property = propertyIndex.replace(name,'').trim();

            if(property!=''){

                kids.push(Hi.$util.lcfirst(property));

            }

        }

    }


    return kids;



};

Hi.$util.camelCase = function (parts){

    var camelCased = '';

    parts.forEach(function(item,index){

        if(index==0){

            camelCased = item;

        }else{

            var firstCapital = item[0].toUpperCase();
            var restLowercase = item.substr(1,item.length-1);

            var unitedCamel = firstCapital+restLowercase;

            camelCased = camelCased+unitedCamel;

        }

    });

    return camelCased;

};


Hi.$util.explode = function(glueChar,string){

    var array = new Array();
    var pendingString ="";

    if(string.trim()==""){

        return [];

    }

    for(var charIndex in string){
        var char = string[charIndex];

        if(typeof char !='string'){

            continue;

        }

        if(char==glueChar){

            if(pendingString!=''){

                array.push(pendingString);
                pendingString="";

            }

        }else{

            pendingString = pendingString+char;

        }

    }

    if(pendingString!=''&&array.length==0){

        array.push(pendingString);

    }else if(array.length>0){

        if(array[array.length-1].valueOf()!=pendingString){

            array.push(pendingString);

        }


    }

    return array;

};




/**
 * - - - - - - - -
 * NAV
 * - - - - - - - -
 */

Hi.$nav.goto = function(element_id,name){

    var route_command = "Hi.$nav.navigateTo('"+name+"');";
    $("#"+element_id).attr('onClick',route_command);

};


//Faz bind do clique ou de outro evento de um elemento a uma rota
Hi.$nav.bind = function(element_id,name,eventName){

    var route_command = "Hi.$nav.navigateTo('"+name+"');";
    if(eventName){

        if(typeof element_id === 'string'){

            $("#"+element_id).bind(eventName,function(){

                eval(route_command);

            });

        }else{

            $(element_id).bind(eventName,function(){

                eval(route_command);

            });

        }


    }else{


        $("#"+element_id).attr('onClick',route_command);

    }


};


Hi.$nav.getViewPath = function(controller,action){

    var vp = {controller:controller,action:action};
    return vp;

};

Hi.$nav.getTextViewPath = function(controller, action){

    return controller+"_"+action;

};

Hi.$nav.routeBack = function(route_name_or_object,getParams){

    Hi.$nav.isGoingBack = true;
    Hi.$nav.navigateTo(route_name_or_object,getParams);


};



Hi.$nav.history = [];
Hi.$nav.last =  false;
Hi.$nav.isGoingBack = false;
Hi.$nav.namedRoutes={};


Hi.$nav.newNamedRoute = function(name,object){

    if(!Hi.$nav.hasOwnProperty(name)){
        Hi.$nav.namedRoutes[name]=object;
        return object;
    }

    return false;

};


Hi.$nav.getNamedRoute = function(name){

    //O parametro eh uma string
    if(typeof name==='string'){

        //Eexiste uma rota com o nome especificado
        if(Hi.$nav.namedRoutes.hasOwnProperty(name)){


            //Pega o objecto da rota
            return Hi.$nav.namedRoutes[name];

        }

        return false;
    }

};



Hi.$nav.setActivePath = function(url){

    Hi.$nav.history.push(url);
    return Hi.$nav.history.length -1;

};


Hi.$nav.getPreviousPath = function(url){

    if(Hi.$nav.history.length>0){

        var theUrl = Hi.$nav.history[0];
        Hi.$nav.history.removeVal(theUrl);
        return theUrl;

    }


    return false;

};



Hi.$nav.active = false;
Hi.$nav.navigateTo = function(route_name_or_object,getParams,embed,callback,$embedScope,embedOptions){

    //There is an active request : just cancel it
    if(Hi.$nav.active){

        if(Hi.$nav.active.hasOwnProperty("abort")){

            Hi.$nav.active.abort();
            Hi.$nav.active = false;

        }

    }

    Hi.$nav.last = {name:route_name_or_object};

    var route_object = Hi.$nav.resolveRoute(route_name_or_object);

    route_object = JSON.parse(JSON.stringify(route_object));

    if(!embed){

        if(__.hasOwnProperty("$onRedirectStart")){

            if(typeof __.$onRedirectStart=="function"){

                __.$onRedirectStart.call(__,route_object);

            }

        }

    }


    //Objecto de rota invalido
    if(!route_object){

        return false;

    }

    //Path da View
    var clean_path = Hi.$nav.getURL(route_object);
    var cachingURL = Hi.$nav.getCachingURL(route_object);

    //Parametros get do request
    if(route_object.get){

        getParams = route_object.get;

    }


    if(getParams){

        route_object['get']=getParams;

    }

    //Gera o path novamente
    var path = Hi.$nav.getURL(route_object);


    if(!route_object.dialog){


        var cached_view = false;

        var server_directives = {};

        //A view esta na cache
        if(Hi.$ui.html.cache.stores(cachingURL)){
            cached_view = Hi.$ui.html.cache.fetch(cachingURL);
            server_directives ['Ignore-View'] = 'true';
        }

        if(Hi.$ui.js.wasControllerLoaded(route_object.controller,route_object.action)){
            server_directives["Ignore-Js"] = 'true';
        }


        if(Hi.i18n.cache.hasDictionary(cachingURL)){

            server_directives["Ignore-i18nMapping"] = 'true';

        }


        if(embed){

            route_object.embedded = true;

        }


        Hi.$nav.requestData(route_object,function(server_response){

            if(server_response.response!=200){

                if(typeof server_response=="string" && embed==false){

                    document.write(server_response);
                    return;

                }

                if(embed){

                    if(typeof embedOptions.onError == "function")
                        embedOptions.onError();

                    return;

                }

                throw new Error("Request to server returned an unexpected result");
                return;

            }

            var context_variables = server_response.data;

            if(context_variables.hasOwnProperty("$dictionary")&&!context_variables.hasOwnProperty("$dictionary-no-cache")){

                Hi.i18n.cache.put(cachingURL,context_variables["$dictionary"]);

            }

            //Commands to be executed on the client-side
            if(context_variables.hasOwnProperty('$invoke')){


                var cmdInvocations = context_variables["$invoke"];
                for(var cmdName in cmdInvocations){

                    var cmdParams = cmdInvocations[cmdName];

                    //If the command returns true then this function returns
                    if(Hi.$ui.js.commands.run(cmdName,cmdParams)){

                        return null;

                    }


                }


            }


            var markup = server_response.view;

            if(server_response.controller) {

                Hi.$nav.setNextControllerInfo(route_object.controller, route_object.action);
                eval(server_response.controller);
                Hi.$ui.js.setLoadedController(route_object.controller, route_object.action);

            }


            //A view nao esta na cache
            if(!Hi.$ui.html.cache.stores(cachingURL)){

                Hi.$ui.html.cache.storeView(cachingURL,markup);

            }else{

                markup = Hi.$ui.html.cache.fetch(cachingURL);

            }

            var controller = route_object.controller;
            var action = route_object.action;

            var viewPath =  Hi.$nav.getViewPath(controller,action);//New

            context_variables.$route = route_object;

            var scopeReceptor = {};
            var generated = Hi.$ui.js.createViewScope(viewPath,context_variables,markup,embed,scopeReceptor,$embedScope,embedOptions);

            if(embed){


                if(typeof callback=="function"){

                    callback.call({},generated);

                }

            }




        },server_directives);

    }


};



//Gera a URL de uma rota
Hi.$nav.getURL= function(route,covw) {

    var route_url = "";
    if(typeof App!="undefined")
        route_url = App.base_url;

    if (route.controller && !route.controller) {

        route_url = route_url + route.controller + "/";

    } else if (route.controller && !route.controller) {

        route_url = route_url + route.controller + "/";

    } else if (route.controller && route.controller) {

        route_url = route_url + route.controller + "/";
    }

    if (route.action) {

        route_url = route_url + route.action;
    }



    //Parametros get
    if(route.get){

        if(typeof route.get ==="string"){

            route_url = route_url+"?"+route.get;

        }else{

            var get_params = Hi.$util.encodeGetParams(route.get);
            route_url = route_url+"?"+get_params;
        }



    }


    return route_url;

};


//Gera a URL de cacheamento de uma rota
Hi.$nav.getCachingURL= function(route,covw) {

    var route_url = "";
    if(typeof App!="undefined")
        route_url = App.base_url;

    if (route.controller && !route.controller) {

        route_url = route_url + route.controller + "/";

    } else if (route.controller && !route.controller) {

        route_url = route_url + route.controller + "/";

    } else if (route.controller && route.controller) {

        route_url = route_url + route.controller + "/";
    }

    if (route.action) {

        route_url = route_url + route.action;
    }

    var getParams = {};
    if(route.get)
        getParams = Hi.$util.URLToJson(route.get);

    if(getParams.hasOwnProperty("$")){

        var viewMode = getParams["$"];
        route_url = route_url +"."+viewMode;

    }



    return route_url;

};


Hi.$nav.currentPage=false;
Hi.$nav.currentRoute = false;

Hi.$nav.setLocation = function (location,route){

    Hi.$nav.currentPage=location;
    Hi.$nav.currentRoute = route;



    if(!Hi.$nav.isGoingBack){

        window.history.pushState(route,Hi.$ui.html.getTitle()+Math.random(),location);

    }
    else
        Hi.$nav.isGoingBack = false;


};

Hi.$nav.resetLocation = function(){

    if(Hi.$nav.currentPage){

        window.history.replaceState(Hi.$nav.currentRoute,Hi.$nav.currentPage+Math.random(),Hi.$nav.currentPage);

    }


};


Hi.$nav.isSameRoute = function(url){

    return Hi.$nav.currentPage===url;

};

Hi.$nav.requestData = function(route,callback,server_directives){

    if(route){

        var storeRequest = true;

        if(route.hasOwnProperty("embedded")){

            storeRequest = false;
            delete route["dembed"];

        }

        var route_url = Hi.$nav.getURL(route);
        var server_response=false;

        var request_params = {};
        server_directives.AJAX_MVC=1;

        if(server_directives){
            request_params = server_directives;
        }

        var errorStatus = false;
        var request = $.ajax({

            url:route_url,
            headers : server_directives,
            success: function(server){


                callback(server);


            },
            error: function(jqXHR ,textStatus,errorThrown){

                if(__.hasOwnProperty("$onRedirectError")){
                    if(typeof __.$onRedirectError=="function")
                        __.$onRedirectError.call(__,route,jqXHR.status,request);
                }


                if(__.hasOwnProperty("$onRedirectFinish")){
                    if(typeof __.$onRedirectFinish=="function"){
                        __.$onRedirectFinish.call(__,route);
                    }
                }

                console.error("controller/action HTTP request failed : "+route_url);
                errorStatus = jqXHR.responseText;

            },

            complete : function(){

                Hi.$nav.active = false;
                if(typeof errorStatus=="string")
                    callback(errorStatus);

            }

        });

        if(storeRequest){

            Hi.$nav.active = request;

        }

    }


};

Hi.$nav.nextControllerInfo = {};
Hi.$nav.setNextControllerInfo = function(controller, action){
    Hi.$nav.nextControllerInfo = {controller:controller,action:action};
};



//Resolve uma rota
Hi.$nav.resolveRoute = function(param){


    if(typeof param==='string'){
        if(param.indexOf('/')!==-1){

            var lastIndex = param.length-1;
            var previousIndex = -1;

            //var parts = new Array();
            //var params = new Array();
            //var params_str = false;

            var controller = false;
            var action = false;

            var pending_word = '';
            var getParams = '';

            // movel/view?name=healy

            for(var charIndex in param){

                var char = param[charIndex];

                if(char!=='/'&&char!=='?'){

                    pending_word = pending_word+char;

                }else{


                    if(previousIndex==-1){

                        throw new Error("Invalid route supplied : "+param);

                    }

                    if(!controller&&!action) {

                        controller = pending_word;
                        pending_word = '';

                    }else if(controller&&!action){

                        action = pending_word;
                        pending_word = '';

                    }else if(controller&&action){

                        pending_word = pending_word+ char;

                    }

                }


                if(charIndex == lastIndex){

                    if(!action&&controller){

                        action = pending_word;

                    }else if(controller&&action){

                        getParams = pending_word;

                    }

                }

                previousIndex = charIndex;

            }


            if(!(controller&&action))
                throw new Error("Invalid route supplied : "+param);


            var route = {};
            route.controller = controller;
            route.action = action;

            if(getParams){

                route.get = getParams;

            }


            return route;

        }

        return Hi.$nav.getNamedRoute(param);

    }else{

        if(param.hasOwnProperty('controller')||param.hasOwnProperty('view')){

            var resolved = JSON.parse(JSON.stringify(param));

            if(param.hasOwnProperty('controller')){
                resolved['controller'] = param.controller;
                delete resolved['controller'];
            }

            if(param.hasOwnProperty('view')){
                resolved['action'] = param.view;
                delete resolved['view'];
            }



            return resolved;
        }

        return param;

    }

};


Hi.$nav.toSlashes = function(route){

    return Hi.$nav.getURL(route);

};


/**
 * FRONTIERS
 */

Hi.$frontiers = {};
Hi.$frontiers.Promise = function(){

    var setTo = {obj:false,prop:false,callback:false};
    var forbiddenCallback = undefined;
    var timeoutCallback = undefined;
    var offlineCallback = undefined;
    var interruptedCallback = undefined;
    var overequestCallback = undefined;
    var catchCallback = undefined;
    var finallyCallback = undefined;

    var request = undefined;


    var getGlobalHandler = function(name){

        if(__.hasOwnProperty("$frontiers")) {

            if (__.$frontiers.hasOwnProperty(name))
                return __.$frontiers[name];

        }

        return undefined;

    };

    var getGlobalHandlers = function(){

        if(__.hasOwnProperty("$frontiers")) {

            return __.$frontiers;

        }

        return undefined;

    };

    this._setRequest = function(req){

        request = req;

    };

    this._setResult = function(data){

        if(typeof setTo.callback=="function")
            setTo.callback.call(this,data);



        this._setRequestFinished();

    };

    this._setRequestFinished = function(){

        if(typeof finallyCallback=="function")

            finallyCallback.call(this);

        else{

            var handler = getGlobalHandler("finally");

            if(typeof handler!="undefined") {

                handler.call(getGlobalHandlers(),this);

            }

        }

    };

    this._setHttpError = function(code){


        if(typeof catchCallback=="function"){

            catchCallback.call(this,code);

        }else{

            var handler = getGlobalHandler("catch");

            if(typeof handler!="undefined") {

                handler.call(getGlobalHandlers(),this,code);

            }

        }

        this._setRequestFinished();

    };

    this._setTimedOut = function(){

        var gTimeoutHandler = getGlobalHandler("timeout");
        var gErrorHandler = getGlobalHandler("catch");

        if(typeof timeoutCallback=="function") {

            timeoutCallback.call(this);

        }else if(gTimeoutHandler=="function"){

            gTimeoutHandler.call(getGlobalHandlers(),this);

        }else if(typeof catchCallback=="function"){

            this._setHttpError(408);

        }else if(typeof gErrorHandler=="function"){

            gErrorHandler.call(getGlobalHandlers(),this,408);

        }

        this._setRequestFinished();

    };

    this._setOffline = function(){

        var gOfflineHandler = getGlobalHandler("offline");
        var gErrorHandler = getGlobalHandler("catch");

        if(typeof offlineCallback=="function") {

            offlineCallback.call(this);

        }else if(typeof gOfflineHandler=="function"){

            gOfflineHandler.call(getGlobalHandlers(),this);

        }else if(typeof catchCallback=="function"){

            this._setHttpError(0);

        }else if(typeof gErrorHandler=="function"){

            gErrorHandler.call(getGlobalHandlers(),this,0);

        }

        this._setRequestFinished();

    };

    this._setForbidden = function(){

        var gForbiddenHandler = getGlobalHandler("forbidden");
        var gErrorHandler = getGlobalHandler("catch");

        if(typeof forbiddenCallback=="function") {

            forbiddenCallback.call(this);

        }else if(typeof gForbiddenHandler=="function"){

            gForbiddenHandler.call(getGlobalHandlers(),this);

        }else if(typeof catchCallback=="function"){

            this._setHttpError(403);

        }else if(typeof gErrorHandler=="function"){

            gErrorHandler.call(getGlobalHandlers(),403);

        }

        this._setRequestFinished();

    };

    this._setInterrupted = function(){

        var gInterruptedHandler = getGlobalHandler("interrupted");
        var gErrorHandler = getGlobalHandler("catch");

        if(typeof interruptedCallback=="function") {

            interruptedCallback.call(this);

        }else if(typeof gInterruptedHandler=="function"){

            gInterruptedHandler.call(getGlobalHandlers(),this);

        }else if(typeof catchCallback=="function"){

            this._setHttpError(452);

        }else if(typeof gErrorHandler=="function"){

            gErrorHandler.call(getGlobalHandlers(),this,452);

        }

        this._setRequestFinished();

    };

    this._setOverRequest = function(){


        var gOverequestHandler = getGlobalHandler("overrequest");
        var gErrorHandler = getGlobalHandler("catch");

        if(typeof overequestCallback=="function") {

            overequestCallback.call(this);

        }else if(typeof gOverequestHandler=="function") {

            gOverequestHandler.call(getGlobalHandlers(), this);

        }else if(typeof catchCallback=="function"){

            this._setHttpError(429);

        }else if(typeof gErrorHandler=="function"){

            catchCallback.call(getGlobalHandlers(),this,429);
        }

        this._setRequestFinished();


    };

    this._setException = function(type){

        var gErrorHandler = getGlobalHandler("catch");

        if(typeof catchCallback=="function") {

            catchCallback.call(this, type);

        }else if(typeof gErrorHandler=="function") {

            gErrorHandler.call(getGlobalHandlers(), this, type);

        }

        this._setRequestFinished();

    };

    //--public interface


    this.try = function(obj) {


        if(typeof obj=="function"){

            setTo.callback = obj;

        }else{

            throw new Error("Wrong parameters");

        }

        return this;

    };

    this.finally = function(obj){

        if(typeof obj=="function"){

            finallyCallback = obj;

        }else{

            throw new Error("Wrong parameters");

        }

        return this;

    };

    this.run = function(){



    };

    this.do = function(){

        this.run();

    };

    this.forbidden = function(callback){

        if(typeof callback!="function")
            throw new Error("Wrong parameters");

        forbiddenCallback = callback;
        return this;

    };

    this.timeout = function(callback){

        if(typeof callback!="function")
            throw new Error("Wrong parameters");

        timeoutCallback = callback;
        return this;

    };

    this.offline = function(callback){

        if(typeof callback!="function")
            throw new Error("Wrong parameters");

        offlineCallback = callback;
        return this;

    };

    this.interrupted = function(callback){

        if(typeof callback!="function")
            throw new Error("Wrong parameters");

        interruptedCallback = callback;
        return this;

    };

    this.overrequest = function(callback){

        if(typeof callback!="function")
            throw new Error("Wrong parameters");

        overequestCallback = callback;
        return this;

    };

    this.catch = function(callback){

        if(typeof callback!="function")
            throw new Error("Wrong parameters");

        catchCallback = callback;
        return this;

    };

    this.getRequest = function(){

        return request;

    };

};


/**
 * -----------
 * Public API
 * -----------
 */

//The upload class
var Upload = function(name,files){

    if(typeof name!=="string"||typeof files!=="object")
        throw new Error("Invalid params supplied to Upload class constructor");

    this.name = name;
    this.files = files;


    this.getName = function(){

        return this.name;

    };

    this.getFiles = function(){

        return this.files;

    };

    this.length = function(){

        return this.files.length;

    }


};



Hi.rootTemplate = function(properties){

    Hi.$ui.js.root = properties;

};

//Regista o controlador do template
Hi.template = function(master){

    jQuery.extend(master,Hi.$ui.js.root);
    Hi.$ui.js.root=master;

};

Hi.redirect = Hi.$nav.navigateTo;
Hi.ajaxify = Hi.$nav.navigateTo;

//Cria um closure javascript para uma determinada view
Hi.view = function(controller){

    //Obtem informacao sobre o controller que esta sendo registrado
    var viewPath = Hi.$nav.nextControllerInfo;

    //Resolved route
    var route = Hi.$nav.resolveRoute(viewPath);

    //Rota correspondente a esta view
    controller.$route = route;

    //Regista o controller
    Hi.$ui.js.setViewController(viewPath.controller,viewPath.action,controller);

};


String.prototype.startsWith = function(str){
    return this.indexOf(str)===0;
};


Array.prototype.removeVal = function(el){
    var array = this;
    var index = array.indexOf(el);
    if (index > -1) {
        array.splice(index, 1);
    }
};


window.onpopstate = function(param){

    if(typeof App=="undefined")
        return;

    var the_base_url = App.base_url;//The base URL

    var the_requested_url = window.location.toString(); //The requested URL

    //The base URL is the same
    if(the_requested_url.startsWith(the_base_url)){

        var lastChar = the_requested_url.charAt(the_requested_url.length-1);

        if(lastChar=="#"){

            return;

        }

        //if(param.state){

        var destination = the_requested_url.replace(App.base_url,"");
        console.info("Destination route : "+destination);

        Hi.$nav.routeBack(destination);

       // }

    }

};

//Internationalization
Hi.i18n = {};
Hi.i18n.dictionary = {};
Hi.i18n.cache = {};
Hi.i18n.cache.key = "";
Hi.i18n.cache.get = function(){

    if(typeof localStorage!="undefined"){
        if(!localStorage.hasOwnProperty(Hi.i18n.cache.key))
            return {$zero:true};
        return JSON.parse(localStorage[Hi.i18n.cache.key]);

    }

    return {$zero:true};
};


Hi.i18n.cache.hasDictionary = function(name){

    return Hi.i18n.cache.get().hasOwnProperty(name);

};

Hi.i18n.cache.put = function(name,dictionary){
    var cache = Hi.i18n.cache.get();
    cache[name] = dictionary;
    Hi.i18n.cache.update(cache);
};

Hi.i18n.cache.update = function(cache){

    if(typeof App == "undefined")
        return;

    if(App.deployMode == "DEVELOPMENT")
        return;

    if(cache.hasOwnProperty("$zero"))
        delete cache["$zero"];

    if(typeof localStorage!="undefined"){
        localStorage[Hi.i18n.cache.key] = JSON.stringify(cache);
    }

};

Hi.i18n.cache.load = function(){

    Hi.i18n.dictionary = Hi.i18n.cache.get();

};

Hi.i18n.cache.destroy = function(){
    if(typeof localStorage!="undefined"){
        if(!localStorage.hasOwnProperty(Hi.i18n.cache.key))
            return;
        delete localStorage[Hi.i18n.cache.key];
    }
};

Hi.i18n.init = function(){

    Hi.i18n.cache.key = "i18n-"+App.base_url;
    var cache =  Hi.i18n.cache.get();
    if(cache.hasOwnProperty("$zero")){

        if(typeof $i18nTemplateBundle!="undefined"){
            Hi.i18n.dictionary = $i18nTemplateBundle;
        }

        if(typeof $i18nBundle !="undefined"){
            Hi.i18n.dictionary = $i18nBundle;
        }

    }else{

        Hi.i18n.dictionary = cache;

    }

};

Hi.i18n.get = function(key){
    if(!Hi.i18n.dictionary.hasOwnProperty(key))
        return key;

    return Hi.i18n.dictionary[key];
};

Hi.i18n.format = function(key, values){

    key = Hi.i18n.get(key);

    for(var  propKey in values){
        var ngKey = "{{"+propKey+"}}";
        var propValue = values[propKey];
        key = key.split(ngKey).join(propValue);
    }

    return key;

};


Hi.i18n.TranslatePromise = function(key){

    this.with = function(values){

        return Hi.i18n.format(key,values);

    }

};


function translate(key){

    if(key.indexOf("{{")!=-1&&key.indexOf("}}")!=-1)
        return new Hi.i18n.TranslatePromise(key);

    return Hi.i18n.get(key);

}

function __t(string,key){

    return translate(key);

}

var $fiis = {};
var fMx = function(params,$functionUrl,_$tout,_$fmut,_$si,_$si_method,_$abpon,fargs) {

    var withUploads = false;

    //This frontier has arguments
    if (fargs.length > 0) {
        for (var index in fargs) {
            var argument = fargs[index];
            if (Upload.prototype.isPrototypeOf(argument)) {
                withUploads = true;
                break;
            }
        }
    }

    var promisse = new Hi.$frontiers.Promise();
    if ($fiis.hasOwnProperty(_$fmut)) {
        if (_$si) {

            if (_$si_method) {

                if (_$abpon) {

                    $fiis[_$fmut].abort();

                } else
                    return new Hi.$frontiers.Promisse();


            } else {


                if (JSON.stringify(params) == $fiis[_$fmut].$params) {

                    if (_$abpon) {

                        $fiis[_$fmut].abort();

                    } else
                        return new Hi.$frontiers.Promise();

                }
            }
        }
    }

    var ajaxParams = {
        success: function (data) {

            if (data.hasOwnProperty("$invoke")) {

                var cmdInvocations = data["$invoke"];
                for (var cmdName in cmdInvocations) {
                    var cmdParams = cmdInvocations[cmdName];
                    Hi.$ui.js.commands.run(cmdName, cmdParams);
                }
            }

            if (data.hasOwnProperty("$root")) {
                if (typeof __ != "undefined") {
                    for (var key in data.$root) {
                        __[key] = data.$root[key];
                    }
                    __.$apply();
                }
            }

            promisse._setResult(data.result);

        },

        error: function (jqXml, errText, httpError) {
            var errorText = jqXml.responseText;
            var exceptionType = undefined;

            try {

                var responseJSON = JSON.parse(errorText);
                if (responseJSON.hasOwnProperty("type") && responseJSON.hasOwnProperty("details")) {
                    promisse._setException(responseJSON);
                    return;
                }

            } catch (err) {

            }


            //Request aborted
            if (errText == "abort") {
                promisse._setInterrupted();
            } else if (errText == "timeout") {
                promisse._setTimedOut();
            } else {

                switch (jqXml.status) {
                    case 403:
                        promisse._setForbidden();
                        break;
                    case 408:
                        promisse._setTimedOut();
                        break;
                    case 421:
                        promisse._setInterrupted();
                        break;
                    case 429:
                        promisse._setOverRequest();
                        break;
                    case   0:
                        promisse._setOffline();
                        break;
                    default :
                        promisse._setException(500);
                        break;
                }
            }
        },
        complete: function () {
            delete $fiis[_$fmut];
        }

    };

    ajaxParams.method = "POST";
    ajaxParams.url = $functionUrl;
    ajaxParams.headers = {csrfToken: App.csrfToken};
    ajaxParams.dataType = "json";
    ajaxParams.cache = false;
    ajaxParams.timeout = _$tout;

    if (!withUploads) {

        ajaxParams.data = JSON.stringify(params);

    } else {

        var form = new FormData();

        var nonUploadArgs = {};
        var uploadArgs = {};

        for (var argIndex in fargs) {
            var argument = fargs[argIndex];

            //This is an upload argument
            if (Upload.prototype.isPrototypeOf(argument)) {

                var uploadName = argument.getName();
                uploadArgs[uploadName] = argument.length();
                nonUploadArgs["arg" + argIndex] = "$$$upload:" + uploadName;
                var files = argument.getFiles();
                for (var i = 0; i < files.length; i++)
                    form.append(uploadName + "_file_" + i, files[i]);

            } else {

                if (typeof argument != "function")
                    nonUploadArgs["arg" + argIndex] = argument;


            }

        }

        form.append("$uploads", JSON.stringify(uploadArgs));
        form.append("$args", JSON.stringify(nonUploadArgs));
        ajaxParams.processData = false;
        ajaxParams.contentType = false;
        ajaxParams.data = form;
    }

    var $req = $.ajax(ajaxParams);
    promisse._setRequest($req);
    $req.$params = JSON.stringify(params);
    $fiis[_$fmut] = $req;
    return promisse;
};