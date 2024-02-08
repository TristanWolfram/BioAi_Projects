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

        for i in range(generation_num):
            print(f"Generation: {i}")
            population_new, fitness_new = self.generate_generation(
                population, crossover_rate, mutation_rate, limitation, crowding
            )
            print(f"Population shape: {population_new.shape}")
            print(f"Fitness: {fitness_new}")
            population = population_new

            generations.append(population_new)
            fitness_per_generation.append(fitness_new)
            average_fitness.append(np.sum(fitness_new) / len(fitness_new))
            max_fitness.append(np.max(fitness_new))
            sum_fitness.append(np.sum(fitness_new))

        print(
            "GA finished with :",
            np.sum(np.array(fitness_per_generation[-1]))
            / len(np.array(fitness_per_generation[-1])),
        )
        return (
            generations,
            fitness_per_generation,
            average_fitness,
            max_fitness,
            sum_fitness,
        )

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

    def deterministic_crowding(
        self, population, crossover_rate, mutation_rate, limitation
    ):

        pop_new = population.copy()
        j = 1

        while j <= population.shape[0]:

            parents = self.roulette_wheel_selection(pop_new, 2, limitation)

            offspring = self.generate_offspring(parents[0], parents[1], crossover_rate)
            print(offspring)

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

    def generate_initial_population(self, population_size, num_features):
        return self.myRNG.integers(
            0, 1, size=(population_size, num_features), endpoint=True
        )

    def create_fitness_scores(self, population, limitation):
        fitness_scores = []
        for i in range(population.shape[0]):

            fitness = self.get_fitness_of_bitstring(population[i], limitation)

            fitness_scores.append(fitness)
        return np.array(fitness_scores)

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

    def select_best(self, population, num_parents):
        fitness_scores = self.create_fitness_scores(population)
        best = np.argsort(fitness_scores)[-num_parents:]
        return population[best]

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

    def mutate_gene(self, bit_string, mutation_rate):
        for i in range(len(bit_string)):
            if self.myRNG.random() < mutation_rate:
                bit_string[i] = 1 - bit_string[i]

        return bit_string
