# Hi-Framework
It's a light Java Framework that allows developers to write fully Ajax web applications combining the power of a Java back-end to a rich client-side powered by AngularJS.


# Need some guidance?
Please read the documentation at [https://docs.hi-framework.org/1.1.0/getting-started/](https://docs.hi-framework.org/1.1.0/getting-started/index.html "Hi-Framework docs")

# 1.4.0 Changes
* Base URL configuration support added
* Application Startup event introduced
* Fixed: Bug that was preventing applications from working on Root Context

## Base URL configuration
### Static configuration approach 
This approach makes use of the __hi.xml__ configurations file.
```xml
    <web>
        ...
        <base-url>http://somewhere.com/</base-url>
        ...
    </web>
```

### Dynamic configuration approach
This approach lets you change the base URL from anywhere in your application.

```java

    ...
    
    @Inject
    AppContext context;


    public void whatever(){
        
        context.setBaseURL("http://somethingelse.com");
        
    }
    
    ...

```

## Startup Event
This event allows to perform some application specific initializations, including defining the Base URL:

```java
    
    public void startup(@Observes ApplicationStartupEvent event){
   
        //TODO: Initialize your app here
   
    }

```