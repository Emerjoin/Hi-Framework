
//TODO: Script load error callback

/**
 * Load scripts and then load the Yapiys script
 */
function ajaxLoad(onstart, onerror, syncscripts, scripts, plugins, beforeIgnition, templateImages, pushUpdates, css){

    //Load started
    if(typeof onstart=="function")
        onstart();

    if(!Array.isArray(syncscripts)){

        syncscripts = [];

    }

    if(!Array.isArray(plugins)){

        plugins = [];

    }

    if(!Array.isArray(templateImages)){

        templateImages = [];

    }

    if(!Array.isArray(scripts)){

        scripts = [];

    }

    if(!Array.isArray(css)){

        css = [];

    }


    var updateStatus = function(value){

        if(typeof value !="undefined"){

            if(typeof pushUpdates=='function'){

                pushUpdates.call({},value);

            }

            return;

        }

        var total = syncscripts.length+scripts.length+templateImages.length+plugins.length+1+css.length;
        var done = sync_scripts_done_loading.length+scripts_done_loading.length+images_loaded.length+plugins_done_loading.length+css_done_loading.length;

        if(total>0&&done>0){

            var percent = ((done/total)*90)+10;

            if(typeof pushUpdates=='function'){

                pushUpdates.call({},percent);

            }

        }


    };


    var waiting_for_images = false;
    var waiting_for_css = false;

    //Load template Images
    var images_loaded = [];


    var async_images_load = function(){


        for(var imageIndex in templateImages){

            var imageSrc = templateImages[imageIndex];


            var element = document.createElement("img");
            element.src = imageSrc;
            element.style="display:none";


            var finished = function(){

                images_loaded.push(this.src);
                updateStatus();

                if(images_loaded.length==templateImages.length){


                    if(waiting_for_images&&!waiting_for_css){

                        all_done();

                    }


                }

            };

            //When finish loading script
            element.onload = finished;
            element.onerror = finished;


        }


    };


    //Load scripts syncronously

    var sync_scripts_done_loading = [];


    //Load next sync script
    var sync_scripts_next = function(){


        if(sync_scripts_done_loading.length<syncscripts.length){


            var sync_script_path = syncscripts[sync_scripts_done_loading.length];

            var element = document.createElement("script");
            element.src = sync_script_path;


            var finished = function(){

                sync_scripts_done_loading.push(this.src);
                updateStatus();

                //Load the next sync script
                sync_scripts_next();


            };

            //When finish loading sync script
            element.onload = finished ;
            element.onerror = function(){

                if(typeof onerror=="function")
                    onerror(this.src);

                return;

            };

            document.body.appendChild(element);


        }else{

            //Finished loading the sync scripts
            //Load the sync scripts
            load_async_scripts();

        }


    };



    var css_done_loading = [];

    var scripts_done_loading = [];

    //Trigger before ingition callback and then ignite yapiys
    var all_done = function(){


        if(typeof  beforeIgnition=='function'){


            beforeIgnition();


        }


        /*
        angular.element(document).ready(function() {


            angular.bootstrap(document, ["hi"]);




        });*/

        $ignition();


    };



    var load_hi_script = function(){


        var element = document.createElement("script");
        element.src = 'hi-es5.js';

        //When finish loading yapiys script
        element.onload = function(){


            //_place_init_code_here

            //Load the yapiys plugin
            load_hi_plugins();


        };

        document.body.appendChild(element);




    };


    var plugins_done_loading = [];


    //Finished loading all other scripts, load yapiys script and then load plugins
    var load_hi_plugins = function(){


        if(!Array.isArray(plugins)){

            plugins = [];

        }


        if(Array.isArray(plugins)){

            //There are no plugins
            if(plugins.length==0){

                //Everthing is done
                if(images_loaded.length==templateImages.length&&css.length==css_done_loading.length){

                    all_done();

                }else{


                    if(images_loaded.length!=templateImages.length)
                        waiting_for_images = true;

                    if(css.length!=css_done_loading.length)
                        waiting_for_css = true;


                }

                return;

            }


            for(var pluginIndex in plugins){

                var pluginPath = plugins[pluginIndex];

                if(typeof pluginPath =='string'){


                    var element = document.createElement("script");
                    element.src = pluginPath;

                    var finished= function(){


                        plugins_done_loading.push(true);
                        updateStatus();

                        //Done loading plugins
                        if(plugins_done_loading.length==plugins.length){

                            //Everything is done
                            if(images_loaded.length==templateImages.length){

                                all_done();

                            }else{

                                waiting_for_images = true;

                            }


                        }

                    };

                    //When finish loading script
                    element.onload = finished;
                    element.onerror=function(){

                        if(typeof onerror=="function")
                        onerror(this.src);
                        return;

                    };

                    document.body.appendChild(element);


                }

            }

        }

    };



    var load_async_scripts = function(){

        if(scripts.length==0){

            load_hi_script();
            return;


        }


        //Load each script
        for(scriptIndex in scripts){

            var scriptPath = scripts[scriptIndex];

            var element = document.createElement("script");
            element.src = scriptPath;

            var finished = function(){

                scripts_done_loading.push(this.src);
                updateStatus();

                if(scripts_done_loading.length==scripts.length){

                    //Load the yapiys javascript
                    load_hi_script();

                }

            };

            //When finish loading script
            element.onload = finished;
            element.onerror = function(){

                if(typeof onerror=="function")
                onerror(this.src);
                return;

            };

            document.body.appendChild(element);

        }


    };


    var load_async_css = function(){

        var documentHead = document.getElementsByTagName('head')[0];

        //Load each css resource
        for(cssIndex in css){

            var cssPath = css[cssIndex];

            var element = document.createElement("link");
            element.type = 'text/css';
            element.rel = 'stylesheet';
            element.href = cssPath;

            documentHead.appendChild(element);

            var finished = function(){

                css_done_loading.push(this.href);
                updateStatus();

                if(css_done_loading.length==css.length){


                    if(waiting_for_css&&!waiting_for_images){

                        all_done();

                    }

                }

            };


            //When finish loading css
            element.onload = finished;
            element.onerror = finished;

            documentHead.appendChild(element);

        }


    };



    if(typeof pushUpdates=="function"){

        pushUpdates(10);

    }


    //Loads all the sync scripts
    sync_scripts_next();

    //Load  template
    async_images_load();

    load_async_css();



}


