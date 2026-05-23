import { useEffect, useState } from "react";
import api from "../utils/axios";

const unwrapApiData = (payload) => {
  if (payload && typeof payload === "object" && "success" in payload && "data" in payload) {
    if (!payload.success) {
      throw new Error(payload.message || "Request failed");
    }
    return payload.data;
  }
  return payload;
};

export const useFetch = (endpoint, dependencies = []) => {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchData = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await api.get(endpoint);
      setData(unwrapApiData(response.data));
    } catch (err) {
      const errorMsg = err.response?.data?.message || err.message || "Failed to fetch data";
      setError(errorMsg);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!endpoint) {
      setData(null);
      setLoading(false);
      return;
    }
    fetchData();
  }, [endpoint, ...dependencies]);

  return { data, loading, error, refetch: fetchData };
};

export const usePost = (endpoint) => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [data, setData] = useState(null);

  const execute = async (payload) => {
    try {
      setLoading(true);
      setError(null);
      const response = await api.post(endpoint, payload);
      const normalized = unwrapApiData(response.data);
      setData(normalized);
      return normalized;
    } catch (err) {
      const errorMsg =
        err.response?.data?.message ||
        err.response?.data?.error ||
        err.message ||
        "Request failed";
      setError(errorMsg);
      throw new Error(errorMsg);
    } finally {
      setLoading(false);
    }
  };

  return { execute, loading, error, data };
};

export default useFetch;
