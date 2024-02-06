import numpy as np
import pandas as pd
import LinReg as lr

class GA:
    def __init__(self):
        self.myRNG = np.random.default_rng()
        self.regressor = lr.LinReg()

        print("Initialized GA")

    def run(self, generation_num, population_size, crossover_rate, mutation_rate, data_set_str):

        data = pd.read_csv(data_set_str, header=None)
        num_features = len(data.columns) - 1

        print(f"started GA with population_size: {population_size}, generation number: {generation_num}, crossover rate: {crossover_rate}, mutation rate: {mutation_rate}")
        print(f"Data set shape: {data.shape}")

        # Initialize population
        population = self.__generate_initial_population(population_size, num_features)
        print(f"initiliazed population with shape: {population.shape}")

        for i in range(generation_num):
            print(f"Generation: {i}")
            population_new = self.__generate_generation(population, crossover_rate, mutation_rate, data)
            print(f"Population shape: {population_new.shape}")
            population = population_new

    def __generate_generation(self, population, crossover_rate, mutation_rate, data):

        # TODO: Implement further, not working now!!

        parents = self.__parent_selection(data, population, 2)

        offspring = self.__generate_offspring(parents[0], parents[1], crossover_rate)

        for i in range(len(offspring)):
            offspring[i] = self.__mutate_gene(offspring[i], mutation_rate)
        
        return offspring   

    def __generate_initial_population(self, population_size, num_features):
        return self.myRNG.integers(0, 1, size=(population_size, num_features), endpoint=True)
    
    def __greate_fitness_scores(self, data, population):
        fitness_scores = []
        for i in range(population.shape[0]):
            X = self.regressor.get_columns(data.values, population[i])
            fitness = self.regressor.get_fitness(X[:,:-1], X[:,-1])
            fitness_scores.append((fitness, i))
        return fitness_scores
    
    def __roulette_wheel_selection(self, data, population, num_parents):
        fitness_scores = self.__greate_fitness_scores(data, population)
        sum_fitness = sum([fitness for fitness, i in fitness_scores])
        selection_prob = np.array([fitness/sum_fitness for fitness, i in fitness_scores])
        cum_prob = np.cumsum(selection_prob)
        print(fitness_scores)

        
        selected_parents = fitness_scores[:num_parents]
        selected_parents = [population[i] for fitness, i in selected_parents]
        return np.array(selected_parents)
    
    def __generate_offspring(self, parent1, parent2, crossover_rate):
        if self.myRNG.random() < crossover_rate:
            crossover_point = self.myRNG.integers(1, len(parent1))
            print(crossover_point)
            offspring1 = np.concatenate([parent1[:crossover_point], parent2[crossover_point:]])
            offspring2 = np.concatenate([parent2[:crossover_point], parent1[crossover_point:]])
            return np.array([offspring1, offspring2])
        else:
            return np.array([parent1, parent2])
    
    def __mutate_gene(self, bit_string, mutation_rate):
        for i in range(len(bit_string)):
            if self.myRNG.random() < mutation_rate:
                bit_string[i] = 1 - bit_string[i]

        return bit_string