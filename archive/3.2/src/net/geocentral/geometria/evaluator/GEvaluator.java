/**
 * Copyright 2000-2013 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.evaluator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import net.geocentral.geometria.evaluator.token.GBinaryOperator;
import net.geocentral.geometria.evaluator.token.GDecimal;
import net.geocentral.geometria.evaluator.token.GFunction;
import net.geocentral.geometria.evaluator.token.GLeftParanthesis;
import net.geocentral.geometria.evaluator.token.GOperator;
import net.geocentral.geometria.evaluator.token.GRightParanthesis;
import net.geocentral.geometria.evaluator.token.GToken;
import net.geocentral.geometria.evaluator.token.GTokenBounds;
import net.geocentral.geometria.evaluator.token.GUnaryOperator;
import net.geocentral.geometria.evaluator.token.GValueToken;
import net.geocentral.geometria.evaluator.token.GVariable;
import net.geocentral.geometria.util.GDictionary;

import org.apache.log4j.Logger;

public class GEvaluator {

    private Collection<? extends GVariable> variables;

    private List<GToken> postfixTokens;

    private GTokenBounds errorTokenBounds;

    private static Logger logger = Logger.getLogger("net.geocentral.geometria");

    public <T extends GVariable> GEvaluator() {
    }

    public <T extends GVariable> GEvaluator(Collection<T> variables) {
        this.variables = variables;
    }

    public double evaluate(String input) throws Exception {
        logger.info(input);
        GTokenizer tokenizer = new GTokenizer(variables);
        Map<GToken, GTokenBounds> infixTokenMap;
        try {
            infixTokenMap = tokenizer.tokenize(input);
        }
        catch (Exception exception) {
            errorTokenBounds = tokenizer.getErrorTokenBounds();
            throw exception;
        }
        postfixTokens = new ArrayList<GToken>();
        infixToPostfix(infixTokenMap, postfixTokens);
        return evaluate(infixTokenMap, postfixTokens);
    }

    public GTokenBounds getErrorTokenBounds() {
        return errorTokenBounds;
    }

    public List<GToken> getPostfixTokens() {
        return postfixTokens;
    }

    // http://en.wikipedia.org/wiki/Shunting_yard_algorithm
    private void infixToPostfix(
            Map<GToken, GTokenBounds> infixTokenMap,
            List<GToken> postfixTokens) throws Exception {
        logger.info(infixTokenMap + ", " + postfixTokens);
        Stack<GToken> stack = new Stack<GToken>();
        for (GToken token : infixTokenMap.keySet()) {
            if (token instanceof GValueToken) {
                postfixTokens.add(token);
                continue;
            }
            if (token instanceof GFunction) {
                stack.push(token);
                continue;
            }
            if (token instanceof GOperator) {
                while (true) {
                    if (stack.isEmpty())
                        break;
                    GToken t = stack.peek();
                    if (!(t instanceof GOperator))
                        break;
                    if ((token instanceof GUnaryOperator
                            || ((GBinaryOperator)token).isRightAssociative())
                                && ((GOperator)token).getPrecedence() <
                                    ((GOperator)t).getPrecedence()
                            || (((GBinaryOperator)token).isLeftAssociative()
                            || ((GBinaryOperator)token).isAssociative())
                                && ((GOperator)token).getPrecedence() <=
                                    ((GOperator)t).getPrecedence()) {
                        stack.pop();
                        postfixTokens.add(t);
                        continue;
                    }
                    else
                        break;
                }
                stack.push(token);
                continue;
            }
            if (token instanceof GLeftParanthesis) {
                stack.push(token);
                continue;
            }
            if (token instanceof GRightParanthesis) {
                while (true) {
                    if (stack.isEmpty()) {
                        errorTokenBounds = infixTokenMap.get(token);
                        logger.info("Unmatched parantheses");
                        throw new Exception(
                                GDictionary.get("UnmatchedParentheses"));
                    }
                    GToken t = stack.peek();
                    if (t instanceof GOperator) {
                        stack.pop();
                        postfixTokens.add(t);
                    }
                    else if (t instanceof GLeftParanthesis) {
                        stack.pop();
                        if (!stack.isEmpty()
                                && stack.peek() instanceof GFunction)
                            postfixTokens.add(stack.pop());
                        break;
                    }
                    else {
                        errorTokenBounds = infixTokenMap.get(token);
                        logger.info("Unmatched parantheses");
                        throw new Exception(
                                GDictionary.get("UnmatchedParentheses"));
                    }
                }
                continue;
            }
        }
        while (!stack.isEmpty()) {
            GToken token = stack.pop();
            if (token instanceof GOperator)
                postfixTokens.add(token);
            else {
                errorTokenBounds = infixTokenMap.get(token);
                logger.info("Misplaced token: " + token);
                throw new Exception(GDictionary.get("MisplacedToken",
                        String.valueOf(token)));
            }
        }
    }

    // http://en.wikipedia.org/wiki/Reverse_Polish_notation
    private double evaluate(Map<GToken, GTokenBounds> tokenMap,
            List<GToken> postfixTokens) throws Exception {
        logger.info(tokenMap + ", " + postfixTokens);
        Stack<GValueToken> stack = new Stack<GValueToken>();
        for (GToken token : postfixTokens) {
            if (token instanceof GValueToken)
                stack.push((GValueToken)token);
            else if (token instanceof GFunction) {
                if (stack.isEmpty()) {
                    errorTokenBounds = tokenMap.get(token);
                    logger.info("Function has no arguments: " + token);
                    throw new Exception(GDictionary.get(
                            "FunctionHasNoArguments", String.valueOf(token)));
                }
                GValueToken t = stack.pop();
                double value = ((GFunction) token).evaluate(t);
                stack.push(new GDecimal(value));
            }
            else if (token instanceof GUnaryOperator) {
                if (stack.isEmpty()) {
                    errorTokenBounds = tokenMap.get(token);
                    logger.info("Operator has no arguments: " + token);
                    throw new Exception(GDictionary.get(
                            "OperatorHasNoArguments", String.valueOf(token)));
                }
                GValueToken t = stack.pop();
                double value = ((GUnaryOperator)token).evaluate(t);
                stack.push(new GDecimal(value));
            }
            else if (token instanceof GBinaryOperator) {
                if (stack.size() < 2) {
                    errorTokenBounds = tokenMap.get(token);
                    logger.info("Operator has not enough arguments: " + token);
                    throw new Exception(GDictionary.get(
                            "OperatorHasNotEnoughArguments",
                            String.valueOf(token)));
                }
                GValueToken t1 = stack.pop();
                GValueToken t2 = stack.pop();
                double value = ((GBinaryOperator)token).evaluate(t2, t1);
                stack.push(new GDecimal(value));
            }
        }
        if (stack.isEmpty()) {
            logger.info("Nothing to evaluate");
            throw new Exception(GDictionary.get("NothingToEvaluate"));
        }
        double value = stack.pop().getValue();
        if (!stack.isEmpty()) {
            GToken token = stack.peek();
            errorTokenBounds = tokenMap.get(token);
            logger.info("Misplaced token: " + token);
            throw new Exception(GDictionary.get("MisplacedToken",
                    String.valueOf(token)));
        }
        return value;
    }
}
