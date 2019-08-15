# Hi-Framework
It's a light Java Framework that allows developers to write fully Ajax web applications combining the power of a Java back-end to a rich client-side powered by AngularJS.


# Need some guidance?
Please read the documentation at [https://docs.hi-framework.org/1.1.0/getting-started/](https://docs.hi-framework.org/1.1.0/getting-started/index.html "Hi-Framework docs")

# 1.5.0 Changes
* Redirect API Introduced

## Redirect API Usage example
See the code snippet below:
```java

    ...
    
    @Inject
    RequestContext context;


    public void whatever(){
        
        context.sendRedirect("people/list");
        
    }
    
    ...

```
The API will perform the redirect regardless of being invoked in the middle of an AJAX Request.