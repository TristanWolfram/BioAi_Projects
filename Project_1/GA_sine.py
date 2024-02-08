import copy

import LinReg as lr
import numpy as np
import pandas as pd


class GA_sine:
    def __init__(self):
        self.myRNG = np.random.default_rng()

        print("Initialized GA for sine function task!")

    def run(
        self,
        num_features,
        generation_num,
        population_size,
        crossover_rate,
        mutation_rate,
        limitation=None,
        crowding=False,
    ):

        print(
            f"started GA with num_features: {num_features}, population_size: {population_size}, generation number: {generation_num}, crossover rate: {crossover_rate}, mutation rate: {mutation_rate}"
        )

        # Initialize population
        population = self.generate_initial_population(population_size, num_features)
        print(f"initiliazed population with shape: {population.shape}")

        generations = []
        fitness_per_generation = []
        average_fitness = []
        max_fitness = []
        sum_fitness = []

        # create new generations until the maximum number of generations is reached
        # either by using generation replacement or deterministic crowding
        for i in range(generation_num):
            print(f"Generation: {i}")
            population_new, fitness_new = self.generate_generation(
                population, crossover_rate, mutation_rate, limitation, crowding
            )
            print(f"Population shape: {population_new.shape}")
            population = population_new

            # variables to store plotting data
            generations.append(population_new)
            fitness_per_generation.append(fitness_new)
            average_fitness.append(np.sum(fitness_new) / len(fitness_new))
            max_fitness.append(np.max(fitness_new))
            sum_fitness.append(np.sum(fitness_new))

        print("GA finished with best value of:", max_fitness[-1])
        return (
            generations,
            fitness_per_generation,
            average_fitness,
            max_fitness,
            sum_fitness,
        )

    # create new generations by using generation replacement or deterministic crowding
    def generate_generation(
        self, population, crossover_rate, mutation_rate, limitation, crowding
    ):

        if crowding:
            pop_new, fitness_new = self.deterministic_crowding(
                population, crossover_rate, mutation_rate, limitation
            )
        else:
            pop_new, fitness_new = self.generation_replacement(
                population, crossover_rate, mutation_rate, limitation
            )

        return pop_new, fitness_new

    # create new generations by using generation replacement
    #
    # select parents out of the old generation using roulette wheel selection
    # create offspring by using crossover and mutation
    # add offspring to the new generation
    # do until the new generation has the same size as the old generation
    def generation_replacement(
        self, population, crossover_rate, mutation_rate, limitation
    ):
        pop_new = []
        j = 1
        while j <= population.shape[0]:

            parents = self.roulette_wheel_selection(population, 2, limitation)

            offspring = self.generate_offspring(parents[0], parents[1], crossover_rate)

            for i in range(len(offspring)):
                offspring[i] = self.mutate_gene(offspring[i], mutation_rate)
                pop_new.append(offspring[i])

            j += 2

        pop_new = np.array(pop_new)
        fitness_new = self.create_fitness_scores(pop_new, limitation)

        return pop_new, fitness_new

    # create new generations by using deterministic crowding
    #
    # creates a copy of the old generation
    # select parents out of the copy using roulette wheel selection
    # create offspring by using crossover and mutation
    # replace parents with offspring if the offspring has a better fitness
    # -> in case the same parent got selected twice, only one offspring will be added (if it is better)
    # do until the new generation has the same size as the old generation
    def deterministic_crowding(
        self, population, crossover_rate, mutation_rate, limitation
    ):

        pop_new = population.copy()
        j = 1

        while j <= population.shape[0]:

            parents = self.roulette_wheel_selection(pop_new, 2, limitation)

            offspring = self.generate_offspring(parents[0], parents[1], crossover_rate)

            for k in range(len(offspring)):
                offspring[k] = self.mutate_gene(offspring[k], mutation_rate)

            fit_p1 = self.get_fitness_of_bitstring(parents[0], limitation)
            fit_p2 = self.get_fitness_of_bitstring(parents[1], limitation)

            fit_o1 = self.get_fitness_of_bitstring(offspring[0], limitation)
            fit_o2 = self.get_fitness_of_bitstring(offspring[1], limitation)

            prob_replacement = 0.5
            if fit_o1 > fit_p1:
                prob_replacement = 1
            elif fit_o1 == fit_p1:
                prob_replacement = 0.5
            else:
                prob_replacement = 0

            if self.myRNG.random() < prob_replacement:
                pop_new = np.delete(
                    pop_new,
                    np.where(np.all(pop_new == parents[0], axis=1))[0][0],
                    axis=0,
                )
                pop_new = np.append(pop_new, [offspring[0]], axis=0)

            if fit_o2 > fit_p2:
                prob_replacement = 1
            elif fit_o2 == fit_p2:
                prob_replacement = 0.5
            else:
                prob_replacement = 0

            if self.myRNG.random() < prob_replacement:
                if np.all(pop_new == parents[1], axis=1).any():
                    pop_new = np.delete(
                        pop_new,
                        np.where(np.all(pop_new == parents[1], axis=1))[0][0],
                        axis=0,
                    )
                    pop_new = np.append(pop_new, [offspring[1]], axis=0)

            j += 2

        fitness_new = self.create_fitness_scores(pop_new, limitation)

        return pop_new, fitness_new

    # create the initial population with given size and number of features
    def generate_initial_population(self, population_size, num_features):
        return self.myRNG.integers(
            0, 1, size=(population_size, num_features), endpoint=True
        )

    # gets a whole generation and returns an array with the fitness of each individual (in the same order as the generation)
    def create_fitness_scores(self, population, limitation):
        fitness_scores = []
        for i in range(population.shape[0]):

            fitness = self.get_fitness_of_bitstring(population[i], limitation)

            fitness_scores.append(fitness)
        return np.array(fitness_scores)

    # gets a bitstring and returns the fitness of the bitstring
    def get_fitness_of_bitstring(self, bit_string, limitation):
        int_value = int("".join(map(str, bit_string)), 2)
        scaled_value = int_value * (128 / 2 ** len(bit_string))

        distance = 0
        if limitation is not None:
            if scaled_value < limitation[0]:
                distance = limitation[0] - scaled_value
            elif scaled_value > limitation[1]:
                distance = scaled_value - limitation[1]

        fitness = np.sin(scaled_value) + 1 - (distance / 100)

        return fitness

    # selects a given number of parents out of the population using roulette wheel selection
    # returns an array with the selected parents
    def roulette_wheel_selection(self, population, num_parents, limitation):
        fitness_scores = self.create_fitness_scores(population, limitation)
        sum_fitness = np.sum(fitness_scores)
        selection_prob = fitness_scores / sum_fitness
        cum_prob = np.cumsum(selection_prob)

        parents = []
        for _ in range(num_parents):
            r = self.myRNG.random()
            index = np.where(cum_prob > r)[0][0]
            parents.append(population[index])

        return np.array(parents)

    # gets two parents and a crossover rate and returns an array with the offspring
    def generate_offspring(self, parent1, parent2, crossover_rate):
        if self.myRNG.random() < crossover_rate:
            crossover_point = self.myRNG.integers(1, len(parent1))
            offspring1 = np.concatenate(
                [parent1[:crossover_point], parent2[crossover_point:]]
            )
            offspring2 = np.concatenate(
                [parent2[:crossover_point], parent1[crossover_point:]]
            )
            return np.array([offspring1, offspring2])
        else:
            return np.array([parent1, parent2])

    # gets a bitstring and a mutation rate and returns the mutated bitstring
    def mutate_gene(self, bit_string, mutation_rate):
        for i in range(len(bit_string)):
            if self.myRNG.random() < mutation_rate:
                bit_string[i] = 1 - bit_string[i]

        return bit_string
