# Hi-Framework
It's a light Java Framework that allows developers to write fully Ajax web applications combining the power of a Java back-end to a rich client-side powered by AngularJS.


# Need some guidance?
Please read the documentation at [https://docs.hi-framework.org/1.1.0/getting-started/](https://docs.hi-framework.org/1.1.0/getting-started/index.html "Hi-Framework docs")

# 1.2.0 Changes
## Gson
* GsonInitEvent introduced - event that allows to set configurations on GsonBuilder:

```java

	public void configureGson(@Observes GsonInitEvent event) {
		
		GsonBuilder builder = event.getBuilder();
		
	}

```

* New instance and static methods introduced to __AppContext__:
	=> Gson createGsonInstance();
	=> GsonBuilder getGsonBuilderInstance();
	=> static GsonBuilder getGsonBuilder()
	=> static Gson createGson();
	=> static void setGsonBuilder(GsonBuilder builder)


