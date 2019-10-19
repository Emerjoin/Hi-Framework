# Hi-Framework
It's a light Java Framework that allows developers to write fully Ajax web applications combining the power of a Java back-end to a rich client-side powered by AngularJS.


# Need some guidance?
Please read the documentation at [https://docs.hi-framework.org/1.1.0/getting-started/](https://docs.hi-framework.org/1.1.0/getting-started/index.html "Hi-Framework docs")

# 1.8.0 Changes
## Frontiers JavaScript API Improvements
* Optional callback on try()
* Prepare callback function introduced
* Global success handler introduced
* Success boolean on finally block
* Frontier call descriptor introduced
* Frontier call Replay introduced


### Optional callback on try
Now you no longer need to supply a callback for the try function when calling a frontier.
```javascript
MyFrontier.something(param1,param2).try()
    .catch('MyException',function(ex){
   		//Do something with the exception
    });
```

### Prepare callback function
Now you can define a callback to be invoked before the frontier call is performed. This callback is also be invoked when the frontier call is retried. The idea is to use this callback to perform some UI initialization.
```javascript
MyFrontier.something(param1,param2).try()
    .prepare(function(){
    	//Do some preparation here
    }).catch('MyException',function(ex){
   		//Do something with the exception
    });
```

### Global success handler
Because now you can make a frontier call without passing a success callback, you
can also define a Global success handler. The Global success handler is defined
on the template as the other frontier global handlers. The Global success handler is only invoked whenever there is no success callback passed to the frontier call.
```javascript

Hi.template({

	$frontiers: {
    	success: function(call){
        	//Handle frontier success here
        }
    }

});
```

### Success boolean on finally block
The finnaly callback now receives a boolean that indicates whether the frontier call ended successfully or not.

```javascript
MyFrontier.something(param1,param2).try(function(obj){
    	//Do something with the object
    }).prepare(function(){
    	//Do some preparation here
    }).catch('MyException',function(ex){
   		//Do something with the exception
    }).finally(function(success){
    	//Do something depending on the outcome
    });
```

### Frontier call descriptor
You can now attach a descriptor to a frontier call. This descriptor will be accessible to all the frontier callbacks and to the Frontier global event handlers. Anything can be passed as a descriptor. It might be an object, a number or just a string.

```javascript
MyFrontier.something(param1,param2).try(function(obj){
    	//Do something with the object
    }).as({id:"something",message:"Doing something now"});
```

### Frontier call Replay
Any frontier call can now be replayed, causing the call to be executed again. A __replay__ function was added to the Frontier call object, making it accessible to all the frontier callbacks and also to the Global event handlers.

#### Replaying a call from a callback
```javascript
MyFrontier.something(param1,param2).try(function(obj){
    	//Do something with the object
    }).catch('MyException',function(ex){
    	//Triggering a replay
   		this.replay();
    });
```

#### Replaying a call from a Global handler
```javascript
Hi.template({

	$frontiers: {
    	catch: function(call,err){
        	//Triggering a replay
   			call.replay();
        }
    }

});
```
