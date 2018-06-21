# GraphQL Server

In this section we are going to setup a GraphQL server using Spring Boot. Before we get started please read about
[GraphQL Execution](https://graphql.github.io/learn/execution/) so that you have a grasp on the terminology we are going
to use.

## Setup
* Import the [graphql-java-server/](graphql-java-server) folder into your favorite IDE.
  * This project uses lombok. For information on how to add this to your IDE, go to [their website](https://projectlombok.org/).
  * If you are using intellij go to File &rarr; New &rarr; Project From Existing Sources &rarr; Import as a Gradle project
* Run the application. You can do this by going to the GraphQLApplication class and selecting run as java application or by
running `./gradlew bootRun` or by using whatever tooling your IDE provides for this sort of thing. If it doesn't start, then
something horribly wrong has happened.

## Quick Tour
You are obsessed with gardening but you can never remember how many and what type of plants you have in your garden. Like
any good programmer, you decide the best way to deal with this problem is to write a little garden inventory application.

###### What is already done for you
* `Garden` and `Plant` JPA entities. Garden has many Plants.
  * Repositories and CRUD services for those entities are also done
  * `hsqldb` is setup as the database.
  * Assemblers to convert those JPA entities into your dtos.
* GraphQL Dependencies
  * `com.graphql-java:graphql-spring-boot-starter`
    * This handles most of the configuration needed to get a GraphQL endpoint up and running.
  * `com.graphql-java:graphql-java-tools`
    * A schema-first tool for graphql-java inspired by graphql-tools for Javascript
    * There a number of other [GraphQL Libraries](https://github.com/graphql-java/awesome-graphql-java) we could have used.
This library is very well supported and so things don't get too confusing that is all we are going to use.
  * `com.graphql-java:graphiql-spring-boot-starter`
    * A graphical interactive in-browser GraphQL IDE that we can use to query our Garden application.
* If you find yourself stuck and the answers aren't helping, check out the [graphql-java-server-answer](graphql-angular-answer) directory
for a complete version of the server.    

## Exercise #1 - Garden Root Query

#### Prerequisites
* Read [GraphQL Schema](https://graphql.org/learn/schema/)

#### Tasks
Your Garden Service layer is amazing but you need to get the data out some how. Like any good hipster, you decide you are
going to use GraphQL. To test the waters lets expose all of the Gardens in our schema.

Since `graphql-java-tools` is schema first we will be creating a schema, and then implementing the schema as we go.

1. Create a file in the `src/main/resources` folder called `schema.graphqls`
2. In the `schema.graphqls` add the following types:
    1. Garden type with the following fields
        * id (required with a type of ID)
        * title (required)
        * description
    2. Query type
        * Gardens
            * Returns a list of the Garden Type
3. Create a Root Query Resolver for the gardens query by doing the following
    * Create a class called `QueryResolver` that implements `GraphQLQueryResolver`
    * The class should be annotated with `@Component`
    * The class should have one public method `getGardens()` which returns a List of GardenDto's from the `GardenService` (You will need to transform it).

#### Testing
* Start your server
  * If it didn't start, check the stacktrace. It is sometimes helpful.
* Go to [http://localhost:8080/graphiql](http://localhost:8080/graphiql) to test your code
  * Alternatively you can enter [http://localhost:8080/graphql](http://localhost:8080/graphql) into your favorite GraphQL
  query tool.
* Write a query to get all the Gardens back and run it to confirm it is working correctly.


<details><summary>Answer</summary><p>

__schema.graphqls__
```graphql
type Garden {
    id: ID!
    title: String!
    description: String
}

type Query {
    gardens: [Garden]!
}
```

__QueryResolver.java__
```java
package com.jimrennie.graphql.devday.graphql.resolver;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import com.jimrennie.graphql.devday.core.service.GardenService;
import com.jimrennie.graphql.devday.graphql.api.GardenDto;
import com.jimrennie.graphql.devday.graphql.assembler.GardenDtoAssembler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class QueryResolver implements GraphQLQueryResolver {

	@Autowired
	private GardenService gardenService;
	@Autowired
	private GardenDtoAssembler gardenDtoAssembler;

	public List<GardenDto> getGardens() {
		return gardenService.findAllGardens()
				.stream()
				.map(gardenDtoAssembler::assemble)
				.collect(Collectors.toList());
	}

}
```

__Query__
```graphql
query {
  gardens {
    id
    title
    description
  }
}
```

__Response__
```json
{
  "data": {
    "gardens": [
      {
        "id": "1",
        "title": "My First Garden",
        "description": "This garden is full of hope, but also full of weeds."
      },
      {
        "id": "5",
        "title": "Herb Garden",
        "description": "Parsley sage rosemary and thyme... and basil and dill"
      }
    ]
  }
}
```

</p></details>

## Exercise #2 - Plant Root Query

#### Tasks
It's great to be able to get all of your gardens, but I want to be able to search for a plant by it's plantType.

Create a Plant Type and a plants query that accepts a plantType (required String) and returns a list of plants in your schema and add the code needed
on the java side to run the query (The query already exists in the PlantService). The resolver should return the PlantDto, not
the Plant entity.

#### Testing
* Restart your server
  * If it didn't start, check the stacktrace. It is sometimes helpful.
* Write a query to get all the plants back with a certain plantType.
  * Sample data: Basil, Tomato, Parsley

<details><summary>Answer</summary><p>

__schema.graphqls__
```graphql
type Garden {
    id: ID!
    title: String!
    description: String
}

type Plant {
    id: ID!
    plantType: String!
    quantity: Int!
}

type Query {
    gardens: [Garden]!
    plants(plantType: String!): [Plant]!
}
```

__QueryResolver.java__
```java
package com.jimrennie.graphql.devday.graphql.resolver;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import com.jimrennie.graphql.devday.core.service.GardenService;
import com.jimrennie.graphql.devday.core.service.PlantService;
import com.jimrennie.graphql.devday.graphql.api.GardenDto;
import com.jimrennie.graphql.devday.graphql.api.PlantDto;
import com.jimrennie.graphql.devday.graphql.assembler.GardenDtoAssembler;
import com.jimrennie.graphql.devday.graphql.assembler.PlantDtoAssembler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class QueryResolver implements GraphQLQueryResolver {

	@Autowired
	private GardenService gardenService;
	@Autowired
	private GardenDtoAssembler gardenDtoAssembler;
	@Autowired
	private PlantService plantService;
	@Autowired
	private PlantDtoAssembler plantDtoAssembler;

	public List<GardenDto> getGardens() {
		return gardenService.findAllGardens()
				.stream()
				.map(gardenDtoAssembler::assemble)
				.collect(Collectors.toList());
	}

	public List<PlantDto> getPlants(String plantType) {
		return plantService.findPlantsByPlantType(plantType)
				.stream()
				.map(plantDtoAssembler::assemble)
				.collect(Collectors.toList());
	}

}
```

__Query__
```graphql
query {
  plants(plantType: "basil") {
    id
    plantType
    quantity
  }
}
```

__Response__
```json
{
  "data": {
    "plants": [
      {
        "id": "4",
        "plantType": "Basil",
        "quantity": 11
      },
      {
        "id": "10",
        "plantType": "Basil",
        "quantity": 4
      }
    ]
  }
}
```

</p></details>

## Exercise #3 - Adding Plants to Garden Root Query

#### Tasks
You are querying your gardens root query and you are starting to get frustrated because you can't figure out which plants
are actually in each garden. You also optionally want to be able to filter by plant type when getting the plants in my gardens.

There are two ways to do this, the smart way and the dumb way. The smart way to do this would be to add a list of plants to
the GardenDto and do the needed transformations. We are going to do this the dumb way for learning purposes.

When you need to get a field that isn't immediately available to the entity you are querying, you can create a resolver
to get the field for you. To do this follow these steps:

1. Create a class that `implements GraphQLResolver<GardenDto>` called `GardenResolver`
2. Add the `@Component` annotation to the class
3. Add a method `getPlants` that accepts a `GardenDto` and returns a list of `PlantDto`
4. Use the PlantService to get the plants based on the `GardenDto` parameter's id.
5. Add plants to your `Garden` Type in your schema and try it out with a query!

Now add the ability to optionally filter the plants by plantType.

#### Testing
Run these two queries to make sure your optional filtering is working correctly.

This should return all plants in each garden
```graphql
query {
  gardens {
    title
    plants {
      plantType
      quantity
    }
  }
}
```

 This should return only basil plants in each garden
 ```graphql
query {
  gardens {
    title
    plants(plantType: "basil") {
      plantType
      quantity
    }
  }
}
 ```

<details><summary>Answer</summary><p>

 __schema.graphqls__
 ```graphql schema
type Garden {
    id: ID!
    title: String!
    description: String
    plants(plantType: String): [Plant]!
}

type Plant {
    id: ID!
    plantType: String!
    quantity: Int!
}

type Query {
    gardens: [Garden]!
    plants(plantType: String!): [Plant]!
}
 ```

 __GardenResolver.java__
 ```java
package com.jimrennie.graphql.devday.graphql.resolver;

import com.coxautodev.graphql.tools.GraphQLResolver;
import com.jimrennie.graphql.devday.core.service.PlantService;
import com.jimrennie.graphql.devday.graphql.api.GardenDto;
import com.jimrennie.graphql.devday.graphql.api.PlantDto;
import com.jimrennie.graphql.devday.graphql.assembler.PlantDtoAssembler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class GardenResolver implements GraphQLResolver<GardenDto> {

	@Autowired
	private PlantService plantService;
	@Autowired
	private PlantDtoAssembler plantDtoAssembler;

	public List<PlantDto> getPlants(GardenDto gardenDto, String plantType) {
		return Optional.ofNullable(plantType)
				.map(type -> plantService.findPlantsByGardenIdAndPlantType(gardenDto.getId(), type))
				.orElseGet(() -> plantService.findPlantsByGardenId(gardenDto.getId()))
				.stream()
				.map(plantDtoAssembler::assemble)
				.collect(Collectors.toList());
	}
}
 ```

 </p></details>

 ## Exercise #4 - Modifying (Mutating) a Plant

 #### Tasks
A nuclear winter has settled in... Plant mutations are abundant... it's time to add new radioactive plants or change your plants characteristics in light of this dystopian legumic future.

The easiest way to perform this madness is to:

1. Create a class that `implements GraphQLMutationResolver` called `MutationResolver`
2. Add the `@Component` annotation to the class,
3. Add methods `addPlant` that accepts a plantType and quantity.
4. Use the PlantService to save the plant.

<details><summary>Answer</summary><p>

__schema.graphqls__
```graphql
type Garden {
    id: ID!
    title: String!
    description: String
}

type Plant {
    id: ID!
    plantType: String!
    quantity: Int!
}

type Query {
    gardens: [Garden]!
    plants(plantType: String!): [Plant]!
}

type Mutation {
    addPlant(gardenId: ID!, plantType: String!, quantity: Int): Plant!
}
```

__MutationResolver.java__
```java
package com.jimrennie.graphql.devday.graphql.resolver;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import com.jimrennie.graphql.devday.core.entity.Plant;
import com.jimrennie.graphql.devday.core.service.PlantService;
import com.jimrennie.graphql.devday.graphql.api.PlantDto;
import com.jimrennie.graphql.devday.graphql.assembler.PlantDtoAssembler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MutationResolver implements GraphQLMutationResolver {

	@Autowired
	private PlantService plantService;
	@Autowired
	private PlantDtoAssembler plantDtoAssembler;

	public PlantDto addPlant(Long gardenId, String plantType, Integer quantity) {
		return plantDtoAssembler.assemble(plantService.savePlant(new Plant().setPlantType(plantType).setQuantity(quantity).setGardenId(gardenId)));
	}
}
```

__Query__
```graphql
mutation {
  addPlant(
    gardenId: 1
    plantType: "RadioactivePotato"
    quantity: 100
  ) {
      plantType
      quantity
  }
}
```

__Response__
```json
{
  "data": {
    "addPlant": {
      "plantType": "RadioactivePotato",
      "quantity": 100
    }
  }
}
```

</p></details>

## Exercise #5 - We need more plants!
As one of the last gardens in the area, people are gravitating to your gardens. You need to plant more ASAP! Add an
`incrementPlantQuantity` mutation that accepts a `plantId` and returns the Plant before it's too late.

## Exercise #6 - Zombies!!!

 #### Tasks
The skies boil grey and blue... lightning rips the broken earth. Moaning drifts from beyond the horizon. Slow lumbering forms are seen in the distance... so slow...

How do we leverage GraphQL to manage performance concerns across the graph - well, we have two ways to do this. The first is simply by the beauty of a graph - if you don't reference the node in the query - you will not traverse the graph to fetch it.

How are we going to speed up the multiple calls to the Zombie Service?

1. Use a CompletableFuture.supplyAsync() within the resolver
2. Use the answer for inspiration on how to complete this exercise.

<details><summary>Answer</summary><p>

__schema.graphqls__
```graphql
type Garden {
    id: ID!
    title: String!
    description: String
    plants(plantType: String): [Plant]!
    zombies(zombieType: String): [Zombie]!
}

type Plant {
    id: ID!
    plantType: String!
    quantity: Int!
}

type Zombie {
    id: ID!
    zombieType: String!
    hitPoints: Int!
}

type Query {
    gardens: [Garden]!
    plants(plantType: String!): [Plant]!
    zombie(zombieType: String!): [Zombie]!
}

type Mutation {
    addPlant(plantType: String!, quantity: Int): Plant!
}
```

__GardenResolver.java__
```java
package com.jimrennie.graphql.devday.graphql.resolver;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.coxautodev.graphql.tools.GraphQLResolver;
import com.jimrennie.graphql.devday.core.service.PlantService;
import com.jimrennie.graphql.devday.core.service.ZombieService;
import com.jimrennie.graphql.devday.graphql.api.GardenDto;
import com.jimrennie.graphql.devday.graphql.api.PlantDto;
import com.jimrennie.graphql.devday.graphql.api.ZombieDto;
import com.jimrennie.graphql.devday.graphql.assembler.PlantDtoAssembler;
import com.jimrennie.graphql.devday.graphql.assembler.ZombieDtoAssembler;

import lombok.Data;
import lombok.experimental.Accessors;

@Component
public class GardenResolver implements GraphQLResolver<GardenDto> {

	@Autowired
	private PlantService plantService;
	@Autowired
	private PlantDtoAssembler plantDtoAssembler;
	@Autowired
	private ZombieService zombieService;
	@Autowired
	private ZombieDtoAssembler zombieDtoAssembler;

	public List<PlantDto> getPlants(GardenDto gardenDto, String plantType) {
		return Optional.ofNullable(plantType)
				.map(type -> plantService.findPlantsByGardenIdAndPlantType(gardenDto.getId(), type))
				.orElseGet(() -> plantService.findPlantsByGardenId(gardenDto.getId()))
				.stream()
				.map(plantDtoAssembler::assemble)
				.collect(Collectors.toList());
	}

	public CompletionStage<List<ZombieDto>> getZombies(GardenDto gardenDto, String zombieType) {
		return CompletableFuture.supplyAsync(()->findZombies(new GardenDtoType().setGarden(gardenDto).setType(zombieType)));
	}

	private List<ZombieDto> findZombies(GardenDtoType gardenDtoType) {
		final long startTime = System.currentTimeMillis();
		return Optional.ofNullable(gardenDtoType.getType())
				.map(type -> zombieService.findZombiesByGardenIdAndZombieType(gardenDtoType.garden.getId(), type))
				.orElseGet(() -> zombieService.findZombiesByGardenId(gardenDtoType.garden.getId()))
				.stream()
				.peek(t -> System.out.println("Time taken for = "+gardenDtoType+" was - "+(System.currentTimeMillis()-startTime)))
				.map(zombieDtoAssembler::assemble)
				.collect(Collectors.toList());
	}

	@Data
	@Accessors(chain = true)
	class GardenDtoType {
		GardenDto garden;
		String type;
	}
}

```

__Query__
```graphql
query {
  gardens {
    title
    plants {
      plantType
      quantity
    }
    zombies {
      zombieType
      hitPoints
    }
  }
}


```

__Response__
The response should call asynchronously across the graph and improve performance.

</p></details>

## Exercise #7 - Plants fight Back!!!

 #### Tasks
The Plants are not just going to take this sitting down! Their newfound mutations provide them with the necessary defense against the nefarious necrotic ne're-do-wells. Create another mutation called

Mutations aren't just there to add items - we can also perform mutations across items:

1. Create a schema mutation called plantHitZombie that passes the zombie ID and the number of hit points to reduce.
2. Implement the underlying Resolver method and entity changes.

<details><summary>Answer</summary><p>

__schema.graphqls__
```graphql
type Garden {
    id: ID!
    title: String!
    description: String
    plants(plantType: String): [Plant]!
    zombies(zombieType: String): [Zombie]!
}

type Plant {
    id: ID!
    plantType: String!
    quantity: Int!
}

type Zombie {
    id: ID!
    zombieType: String!
    hitPoints: Int!
}

type Query {
    gardens: [Garden]!
    plants(plantType: String!): [Plant]!
    zombie(zombieType: String!): [Zombie]!
}

type Mutation {
    addPlant(gardenId: ID!, plantType: String!, quantity: Int): Plant!
    incrementPlantQuantity(plantId: ID!): Plant!
    plantHitZombie(zombieId: ID!, hitPoints: Int): Zombie!
}
```

__MutationResolver.java__
```java
package com.jimrennie.graphql.devday.graphql.resolver;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import com.jimrennie.graphql.devday.core.entity.Plant;
import com.jimrennie.graphql.devday.core.service.PlantService;
import com.jimrennie.graphql.devday.core.service.ZombieService;
import com.jimrennie.graphql.devday.graphql.api.PlantDto;
import com.jimrennie.graphql.devday.graphql.api.ZombieDto;
import com.jimrennie.graphql.devday.graphql.assembler.PlantDtoAssembler;
import com.jimrennie.graphql.devday.graphql.assembler.ZombieDtoAssembler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MutationResolver implements GraphQLMutationResolver {

	@Autowired
	private PlantService plantService;
	@Autowired
	private PlantDtoAssembler plantDtoAssembler;
	@Autowired
	private ZombieService zombieService;
	@Autowired
	private ZombieDtoAssembler zombieDtoAssembler;

	public PlantDto addPlant(Long gardenId, String plantType, Integer quantity) {
		return plantDtoAssembler.assemble(plantService.savePlant(new Plant().setPlantType(plantType).setQuantity(quantity).setGardenId(gardenId)));
	}

	public PlantDto incrementPlantQuantity(Long plantId) {
		plantService.incrementPlantQuantity(plantId);
		return plantDtoAssembler.assemble(plantService.getPlantById(plantId));
	}

	public ZombieDto plantHitZombie(Long zombieId, Integer hitPoints) {
		return zombieDtoAssembler.assemble(zombieService.injureZombie(zombieId, hitPoints));
	}

}

```

__Query__
```graphql
mutation {
  addPlant(
    gardenId: 1
    plantType: "RadioactivePotato"
    quantity: 100
  ) {
      plantType
      quantity
  }
}
```

</p></details>

## Exercise #8 - It's a Cemetery Here!!!

 #### Tasks
A few zombies here and there is manageable... what if an entire colony of the same type of Zombies are needed for different reasons. Does it make sense to fetch them one at a time? What if it's the same Zombie reviving itself over and over again.

If a node that has a high performance cost (or multiple calls in a query)... and perhaps this node is traversed multiple times (child) with a different key (N+1) there are ways to mitigate a performance hit through batching/cacheing. Facebook provides a library called "Dataloader" to work with graphql-js to help out.
[https://github.com/facebook/dataloader](https://github.com/facebook/dataloader)

Fortunately There is also a graphql-java port of this library. It also provides convenient utility for gathering statistics on the cache hits for the batching of calls.
[https://github.com/graphql-java/java-dataloader](https://github.com/graphql-java/java-dataloader)

This is an advanced question which means I never got it implemented properly... Let's see if we anyone can implement it!
