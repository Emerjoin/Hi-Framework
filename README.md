# Hi-Framework
It's a light Java Framework that allows developers to write fully Ajax web applications combining the power of a Java back-end to a rich client-side powered by AngularJS.


# Need some guidance?
Please read the documentation at [https://docs.hi-framework.org/1.1.0/getting-started/](https://docs.hi-framework.org/1.1.0/getting-started/index.html "Hi-Framework docs")

# 1.6.0 Changes
* New Security Configurations Introduced
* Content Expiration exception introduced


## Content expiration event
See snippet below:

```javascript

	Hi.template({

		...

		$frontiers:{

        	...

        	//Handle content expiration exception
	        expired : function(call){
	            alert("Content expired");
	        }

	        ...

	    }

	    ...

	});


```


## Security configurations

### Frontiers security configurations
See the __Hi.xml__ snippet below:
```xml

    ...
    
    <frontiers>
        <default-timeout>1600</default-timeout>
        <security>
            <cross-site-request-forgery>
                <token>
                    <jwt-algorithm>HS512</jwt-algorithm>
                    <jwt-passphrase>4d1138af-18da-43fc-b4f5-e4bbebbc13d1</jwt-passphrase>
                    <secure-random-size>25</secure-random-size>
                </token>
                <cookie>
                    <secure>false</secure>
                    <http-only>true</http-only>
                </cookie>
            </cross-site-request-forgery>
        </security>
    </frontiers>
    
    ...

```

### General security configurations
See the __Hi.xml__ snippet below:
```xml

    ...
    
    <security>
        <content-security-policy>
            <deny-iframe-embedding>true</deny-iframe-embedding>
            <block-mixed-content>false</block-mixed-content>
            <policy-allow>
                <navigation to="http://myserver.com http://anotherserver.com *.facebook.com"/>
                <!--default-src-->
                <content from="'self'">
                    <!--img-src-->
                    <images from="'self' *.myserver.com"/>
                    <!--script-src-->
                    <scripts from="'self' *.myserver.com 'unsafe-inline' https://cdnjs.com"/>
                    <!--style-src-->
                    <styles from="'self' 'unsafe-inline'"/>
                </content>
            </policy-allow>
        </content-security-policy>
    </security>
    
    ...

```






The API will perform the redirect regardless of being invoked in the middle of an AJAX Request.