import ast
import json

import matplotlib.pyplot as plt
import numpy as np

training_set = 9

# load patients
file_path_train = f"Project_2/training/train_{training_set}.json"
with open(file_path_train, "r") as json_file:
    data = json.load(json_file)
patients = data["patients"]

# load solution
file_path_solution = f"Project_2/output/train_{training_set}_output.txt"
with open(file_path_solution, "r") as file:
    solution = file.read()
solution_list = ast.literal_eval(solution)


# Create a dictionary of routes
routes = {}
for i in range(len(solution_list)):
    route = []
    for j in range(len(solution_list[i])):
        route.append(
            [
                patients[str(solution_list[i][j])]["x_coord"],
                patients[str(solution_list[i][j])]["y_coord"],
            ]
        )
    routes["route" + str(i + 1)] = np.array(route)

# Depot location
depot = data["depot"]
depot = np.array([depot["x_coord"], depot["y_coord"]])  # Example depot location

# Plot
plt.figure(figsize=(15, 9))

# Plot each route in a different color
for route in routes.values():
    plt.plot(*zip(*np.vstack([depot, route, depot])), marker="o")

# Highlight the depot
plt.plot(depot[0], depot[1], "ko", markersize=10)

plt.title("Route Visualization")
plt.xlabel("X Coordinate")
plt.ylabel("Y Coordinate")
plt.grid(True)
plt.show()
