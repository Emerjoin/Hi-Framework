# Hi-Framework
It's a light Java Framework that allows developers to write fully Ajax web applications combining the power of a Java back-end to a rich client-side powered by AngularJS.


# Need some guidance?
Please read the documentation at [https://docs.hi-framework.org/1.1.0/getting-started/](https://docs.hi-framework.org/1.1.0/getting-started/index.html "Hi-Framework docs")

# 1.1.0 Changes
* i18n support added
* @Template 
* Frontiers timeout control via hi.xml and @Timeout
* Hi-es5.js obfuscated in PRODUCTION mode
* ajaxify now sets href=javascript:void(0)
## New APIS
* Hi.reload(args)
* FrontierRequestEvent.overrideResult(value)
* FrontierRequestEvent.interupt()
* ControllerRequestEvent.interupt()
* FrontEnd.putOnTemplate(key, value)