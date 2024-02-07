import numpy as np
import pandas as pd
import LinReg as lr

class GA_fe_sel:
    def __init__(self):
        self.myRNG = np.random.default_rng()
        self.regressor = lr.LinReg()

        print("Initialized GA for feature selection task!")

    def run(self, data_str, generation_num, population_size, crossover_rate, mutation_rate):

        data = pd.read_csv(data_str)
        num_features = data.shape[1] - 1

        print(f"started GA with data: {data.shape}, num_features: {num_features}, population_size: {population_size}, generation number: {generation_num}, crossover rate: {crossover_rate}, mutation rate: {mutation_rate}")

        # Initialize population
        population = self.generate_initial_population(population_size, num_features)
        print(f"initiliazed population with shape: {population.shape}")

        generations = []
        fitness_per_generation = []

        for i in range(generation_num):
            i += 1
            print(f"Generation: {i}")
            population_new, fitness_new = self.generate_generation(data, population, crossover_rate, mutation_rate)
            print(f"Population shape: {population_new.shape}")
            print(f"Fitness: {fitness_new}")
            population = population_new

            generations.append(population_new)
            fitness_per_generation.append(fitness_new)


        print("GA finished with :", np.sum(np.array(fitness_per_generation[-1])) / len(np.array(fitness_per_generation[-1])))
        return generations, fitness_per_generation

    def generate_generation(self, data, population, crossover_rate, mutation_rate):

        # TODO: Implement further, not working now!!

        pop_new = []
        j = 1
        while j <= population.shape[0]:

            parents = self.select_best(data, population, 2)

            offspring = self.generate_offspring(parents[0], parents[1], crossover_rate)

            for i in range(len(offspring)):
                offspring[i] = self.mutate_gene(offspring[i], mutation_rate)
                pop_new.append(offspring[i])

            j += 2
        
        pop_new = np.array(pop_new)
        fitness_new = self.greate_fitness_scores(data, pop_new)

        return pop_new, fitness_new

    def generate_initial_population(self, population_size, num_features):
        return self.myRNG.integers(0, 1, size=(population_size, num_features), endpoint=True)
    
    def greate_fitness_scores(self, data, population):
        fitness_scores = []
        for i in range(population.shape[0]):
            X = self.regressor.get_columns(data.values, population[i])
            fitness = self.regressor.get_fitness(X[:,:-1], X[:,-1])
            fitness_scores.append(fitness)
        return np.array(fitness_scores)
    
    def roulette_wheel_selection(self, data, population, num_parents):
        fitness_scores = self.greate_fitness_scores(data, population) 
        sum_fitness = np.sum(fitness_scores)
        selection_prob = fitness_scores / sum_fitness
        cum_prob = np.cumsum(selection_prob)

        parents = []
        for _ in range(num_parents):
            r = self.myRNG.random()
            index = np.where(cum_prob > r)[0][0]
            parents.append(population[index])
        
        return np.array(parents)
    
    def select_best(self, data, population, num_parents):
        fitness_scores = self.greate_fitness_scores(data, population)
        best = np.argsort(fitness_scores)[:-num_parents]
        return population[best]
    
    def generate_offspring(self, parent1, parent2, crossover_rate):
        if self.myRNG.random() < crossover_rate:
            crossover_point = self.myRNG.integers(1, len(parent1))
            offspring1 = np.concatenate([parent1[:crossover_point], parent2[crossover_point:]])
            offspring2 = np.concatenate([parent2[:crossover_point], parent1[crossover_point:]])
            return np.array([offspring1, offspring2])
        else:
            return np.array([parent1, parent2])
    
    def mutate_gene(self, bit_string, mutation_rate):
        for i in range(len(bit_string)):
            if self.myRNG.random() < mutation_rate:
                bit_string[i] = 1 - bit_string[i]

        return bit_string