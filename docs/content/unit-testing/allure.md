# Allure framework

The [Allure Framework](https://github.com/allure-framework) is used to convert the `junit xml report` to a website.

<<work in progress, add documentation...>>

Allure is integrated in the maven phase `test` and `verify`. 

The build can be forced with:

```
# Run junit test
mvn test
# Convert the generated junit xml report to a website
mvn allure:report 
```

## Show JUNIT reports

```
mvn allure:serve
```

This will start a local webserver on a free port, and opens a browser with the allure page.

![](images/allure.png)

Multiple views on the unit tests:

![](images/allure1.png)
