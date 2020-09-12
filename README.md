# IMPORTANT NOTICE
This project has moved to Gitlab: https://gitlab.com/emerjoin-oss/hi-framework/core.

# Hi-Framework
It's a light Java Framework that allows developers to write fully Ajax web applications combining the power of a Java back-end to a rich client-side powered by AngularJS.


# Need some guidance?
Please read the documentation at [https://docs.hi-framework.org/1.1.0/getting-started/](https://docs.hi-framework.org/1.1.0/getting-started/index.html "Hi-Framework docs")

# 1.9.0 Changes
* WebEvents Introduced
* Content-expiry event handler decoupled from frontiers
* Apache-tika updated to 1.22 (security vulnerability fixed)


## WebEvents
Now you can fire events from the __backend__ and handle them on the __views__.

### Backend
Creating an event object is as simple as the following example:
```java
    public ExampleEvent extends WebEvent {
    	//Your properties here
    }
```
Events are published to __channels__ and every user has his own private channel. A part from his private channel, a user can also be subscribed to other channels. Multiple users can be subscribed to the same channel at the same time. Channels with multiple users subscribed are considered __groups__.

The name of the user's private channel must be defined via __ActiveUser__. This is the same object used to subscribe/unsubscribe the user to/from channels.

#### Defining user's private channel
Take a look at the code snippet below to learn how to define the user's private channel:

```java
    public class Whatever {
    
    	@Inject
    	private ActiveUser user;
	
	public void setPrivateChannelName(){
            user.setWebEventChannel("john.ive");
        }       
    
    }
```
In the code snippet above we define "zeus" as the current user's private channel.

#### Subscribing users to channels
Let's now learn how to subscribe a user to a channel and also how to later unsubscribe:

```java
    public class Whatever {
    
    	@Inject
    	private ActiveUser user;
              
	public void joinCorporateChannel(){
             user.subscribe("corporate");
        }
        
        public void quitCorporateChannel(){
             user.unsubscribe("corporate");
        }
    
    }
```

#### Publishing events
Let's publish an event to be received by every user that is online:

```java
    public class Whatever {
    
        @Inject
        private WebEventsContext eventsContext;

        public void example(){
            //Performs a broadcast
            eventsContext.publish(new ExampleEvent());
        }
    
    }   
    
```
Let's now publish an event to a limited set of users:
```java
    public class Whatever {
    
        @Inject
        private WebEventsContext eventsContext;

        public void example(){
            //Publish event to some users
            eventsContext.publish(new ExampleEvent(),"john.ive",
            "bill.gates","clarke.griffin");
        }
    
    }   
    
```
Let's now publish an event to a bunch of groups and users:
```java
    public class Whatever {
    
        @Inject
        private WebEventsContext eventsContext;

        public void example(){
            //Publish event to groups and users at the same time
            eventsContext.publish(new ExampleEvent(),"john.ive",
            "corporate","bill.gates","admins");
        }
    
    }   
    
```
You can publish an event to as many channels as you want at the same time, regardless of whether the __channel__ is __private__ or a __group__.

### Frontend
The center of all attentions on the frontend are __views__. Let's now learn how to handle events on views.

#### Handling events on views
The snippet presents how to handle events on view controllers:

```javascript

    Hi.view(function($scope){
    	
        $scope.$on("ExampleEvent",function(event){
        	//Handle event here
        });
        
        $scope.$on("AnotherEvent",function(event){
        	//Handle event here
        });
        
    });
```

#### Handling events from anywhere
You might need to handle an event from the template or even from a custom script in your application. Here is how to proceed:

```javascript
    Hi.$on("ExampleEvent",function(event){
    	//Handle event here
    });
```

#### Backend Connectivity status
Your application can react to backend connectivity status changes. See examples below:

```javascript
    Hi.$events.ready(function(){
       console.log("Events ready");
    });

    Hi.$events.offline(function(){
       console.warn("Disconnected");
    });
````

### Configurations
There is one application-level configuration that you are allowed to do for __web-events__: the __reconnect-interval__. Checkout the configuration snippet below:
```xml
<?xml version="1.0" encoding="UTF-8" ?>
<app xmlns="http://hi-framework.org/XML/1.9.0" 
xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
xsi:schemaLocation="http://hi-framework.org/XML/1.6.0 http://hi-framework.org/xml/Schema_1_9_0.xsd">

    ...

    <events>
        <reconnect-interval>5000</reconnect-interval>
    </events>
    
    ...
</app>

```

### Getting started
To start using the __Web-Events__ feature you need to include a __provider__ in your application. The Hi-Framework team worked on a provider to get you started. Here are the Maven coordinates of the provider:

```xml
    <dependency>
         <groupId>org.emerjoin.hi.web.events</groupId>
         <artifactId>vm-scoped-provider</artifactId>
         <version>1.0.0</version>
    </dependency>
```

This ready-made provider won't work well in a scenarion where you have multiple instances of your application deployed, hence the name __vm_scoped_provider__. This provider assumes that all the event listeners are connected to the same application instance, thus assuming a single-instance deployment.

#### Why have providers?
The __provider__ abstraction allowed __Hi-Framework__ to focus on implementing the events management __core infrastructure__:
* Listeners management: authentication, reconnection
* Events delivery: marshalling and transmission
* Listeners grouping: channels
* Events handling: Javascript-API

The events infrastructure implemented by Hi-Framework works entirely on an __application-instance__ level. When Hi-Framework is asked to deliver a __WebEvent__, it will deliver that event to all listeners that are connected to the current __application-instance__. The __provider__ abstraction creates the possibility of informing all active __application-instances__ to deliver that event. The same applies to channels subscriptions. When Hi-Framework is asked to subscribe the current user to a specific channel, it will only subscribe the listeners that are connected to the current __application-instance__. Providers are meant to implement cluster-awareness on top of the __events infrastructure__ provided by Hi-Framework.


### Implementing a provider
The management of the event consumers and the delivery of the event are both managed at the __Hi-Framework__ level by the __WebEventsController__. A __Web-events provider__ acts as an intermediate between the __application__ and the __WebEventsController__. 
Every behaviour triggered on the __application-level__ is only made effective by Hi-Framework __WebEventsController__, going through a __Web-events provider__: 
* Channel subscription (join/quit)
* Events publishing.


#### Channel subscription
When an application calls __ActiveUser.subscribe("channel")__ or __ActiveUser.unsubscribe("channel")__ the following CDI events are fired respectively:
* JoinChannel
* QuitChannel
These events must be handled by the __provider__.


#### Events publishing
Because __WebEvents__ are published using the __WebEventsContext__ abstraction, a __Web-events provider__ is required to provide a __WebEventsContext__ CDI bean.

#### Example:
The code snippet below presents the __vm_scoped_provider__ implementation:
```java
@ApplicationScoped
public class VMScopedWebEventsContext implements WebEventsContext {

    @Inject
    private WebEventsController eventsController;

    @Override
    public void publish(WebEvent event) {
        WebEventPublishRequest request = new WebEventPublishRequest(event);
        eventsController.execute(request);
    }

    @Override
    public void publish(WebEvent event, String... channels) {
        WebEventPublishRequest request = new WebEventPublishRequest(event, channels);
        eventsController.execute(request);
    }

    public void onJoinChannel(@Observes JoinChannel event){
        eventsController.joinChannel(event.getUser().getUniqueId(),
                event.getChannel());
    }

    public void onQuitChannel(@Observes QuitChannel event){
        eventsController.quitChannel(event.getUser().getUniqueId(),event.
                getChannel());
    }

}
```


## Content-expiry handler
Hi-Framework now allows you to handle content-expiration properly. You can now define a function on your template, to be invoked whenever the backend detects that front-end is __outdated__, meaning that it requires a __reload__. See the example below:

```javascript
    Hi.template({
    	...
        
    	$expired: function(){
        	console.warn("Content expired");
        }
        ...
    });
```
