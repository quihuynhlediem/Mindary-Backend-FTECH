import express from "express";
import jwt from "jsonwebtoken";
import { deleteUserById, getUserById, getUsers, User } from "../models/User";

interface JwtPayload {
  id: string;
}

export const getAllUsers = async (
  req: express.Request,
  res: express.Response
) => {
  try {
    const users = await getUsers();
    return res.status(200).json(users);
  } catch (error) {
    console.log(error);
    return res.sendStatus(400);
  }
};

export const deleteUser = async (
  req: express.Request,
  res: express.Response
) => {
  try {
    const { id } = req.params;

    const deletedUser = await deleteUserById(id);

    return res.json(deletedUser);
  } catch (error) {
    console.log(error);
    return res.sendStatus(400);
  }
};

export const updateUser = async (
  req: express.Request,
  res: express.Response
) => {
  try {
    const { id } = req.params;
    const { username } = req.body;

    if (!username) {
      return res.sendStatus(400);
    }

    const user = await getUserById(id);

    user.username = username;
    await user.save();

    return res.status(200).json(user).end();
  } catch (error) {
    console.log(error);
    return res.sendStatus(400);
  }
};

export const getUserProfile = async (
  req: express.Request,
  res: express.Response
) => {
  const token = req.headers.authorization?.split(" ")[1];
  console.log(token)

  if (!token) {
    res.status(401).json({ message: "Not authorized, no token" });
  }

  try {
    const decoded = jwt.verify(token, process.env.JWT_SECRET) as JwtPayload;
    const user = await User.findById(decoded.id).select("-password");
    console.log(decoded)

    if (!user) {
      res.status(404).json({ message: "User not found" });
    }

    res.status(200).json({
      username: user.username,
      email: user.email,
    });
  } catch (error) {
    res.status(401).json({ message: "Not authorized, token failed" });
    return;
  }
};