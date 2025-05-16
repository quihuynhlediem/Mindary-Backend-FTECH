import express from 'express';

import authentication from "./authentication";
import user from "./user";
import analysis from "./analysis";

const router = express.Router();

export default (): express.Router => {
  authentication(router);
  user(router);
  analysis(router);
  return router;
};