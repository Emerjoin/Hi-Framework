# Hi-Framework
It's a light Java Framework that allows developers to write fully Ajax web applications combining the power of a Java back-end to a rich client-side powered by AngularJS.


# Need some guidance?
Please read the documentation at [https://docs.hi-framework.org/1.1.0/getting-started/](https://docs.hi-framework.org/1.1.0/getting-started/index.html "Hi-Framework docs")

# 1.7.0 Changes
* Frontier calls new exceptions handling approach


## Frontier exceptions handling approach
Now you can handle specific exceptions in a very clean way.
See snippet below:

```javascript

	MyFrontier.something(param1,param2).try(function(){
    	//Successful call reaction comes here
    }).catch("Exception1",function(ex){
    	//Some behavior here
    }).catch("Exception2",function(ex){
   		//Some behavior here
    }).catch(function(ex){
    	//Some behavior here;  
    });


```