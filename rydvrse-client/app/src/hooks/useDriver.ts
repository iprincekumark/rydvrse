/**
 * useDriver Hook
 * Handles driver operations and state management
 */

import { useState, useCallback } from 'react';
import { driverService } from '@/services';
import { useDriverStore, useUIStore } from '@/store';
import type {
  Driver,
  DriverDocument,
  DriverEarnings,
  DocumentType,
  PagedResponse,
  Trip,
} from '@/types';
import { DriverAvailability } from '@/types';

export const useDriver = () => {
  const [isLoading, setIsLoading] = useState(false);
  
  const {
    driver,
    setDriver,
    setIsOnline,
    setAvailability,
    addDocument,
    setEarnings,
    setDocuments,
  } = useDriverStore();
  
  const { addToast } = useUIStore();

  /**
   * Get driver's profile
   */
  const getProfile = useCallback(async (): Promise<Driver | null> => {
    setIsLoading(true);
    try {
      const profile = await driverService.getMyProfile();
      setDriver(profile);
      return profile;
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Failed to fetch profile';
      addToast({ type: 'error', message });
      return null;
    } finally {
      setIsLoading(false);
    }
  }, [setDriver, addToast]);

  /**
   * Toggle driver availability (online/offline)
   */
  const toggleAvailability = useCallback(async (available: boolean): Promise<boolean> => {
    if (!driver) return false;
    
    setIsLoading(true);
    try {
      await driverService.toggleAvailability(driver.id, available);
      setIsOnline(available);
      setAvailability(available ? DriverAvailability.ONLINE : DriverAvailability.OFFLINE);
      
      addToast({
        type: 'success',
        message: available ? 'You are now online' : 'You are now offline',
      });
      return true;
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Failed to update availability';
      addToast({ type: 'error', message });
      return false;
    } finally {
      setIsLoading(false);
    }
  }, [driver, setIsOnline, setAvailability, addToast]);

  /**
   * Upload driver document
   */
  const uploadDocument = useCallback(async (
    documentType: DocumentType,
    documentUrl: string,
    documentNumber?: string
  ): Promise<DriverDocument | null> => {
    if (!driver) return null;
    
    setIsLoading(true);
    try {
      const doc = await driverService.uploadDocument(driver.id, {
        documentType,
        documentUrl,
        documentNumber,
      });
      addDocument(doc);
      
      addToast({
        type: 'success',
        message: 'Document uploaded successfully',
      });
      return doc;
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Failed to upload document';
      addToast({ type: 'error', message });
      return null;
    } finally {
      setIsLoading(false);
    }
  }, [driver, addDocument, addToast]);

  /**
   * Get driver's documents
   */
  const getDocuments = useCallback(async (): Promise<DriverDocument[]> => {
    if (!driver) return [];
    
    setIsLoading(true);
    try {
      const docs = await driverService.getDocuments(driver.id);
      setDocuments(docs);
      return docs;
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Failed to fetch documents';
      addToast({ type: 'error', message });
      return [];
    } finally {
      setIsLoading(false);
    }
  }, [driver, setDocuments, addToast]);

  /**
   * Get driver's earnings
   */
  const getEarnings = useCallback(async (): Promise<DriverEarnings | null> => {
    if (!driver) return null;
    
    setIsLoading(true);
    try {
      const earnings = await driverService.getEarnings(driver.id);
      setEarnings(earnings);
      return earnings;
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Failed to fetch earnings';
      addToast({ type: 'error', message });
      return null;
    } finally {
      setIsLoading(false);
    }
  }, [driver, setEarnings, addToast]);

  /**
   * Get driver's trip history
   */
  const getTripHistory = useCallback(async (
    page: number = 0,
    size: number = 20
  ): Promise<PagedResponse<Trip> | null> => {
    if (!driver) return null;
    
    setIsLoading(true);
    try {
      const trips = await driverService.getTripHistory(driver.id, page, size);
      return trips;
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Failed to fetch trip history';
      addToast({ type: 'error', message });
      return null;
    } finally {
      setIsLoading(false);
    }
  }, [driver, addToast]);

  /**
   * Update driver location
   */
  const updateLocation = useCallback(async (latitude: number, longitude: number): Promise<boolean> => {
    if (!driver) return false;
    
    try {
      await driverService.updateLocation(driver.id, latitude, longitude);
      return true;
    } catch (error) {
      console.error('Failed to update location:', error);
      return false;
    }
  }, [driver]);

  return {
    isLoading,
    driver,
    getProfile,
    toggleAvailability,
    uploadDocument,
    getDocuments,
    getEarnings,
    getTripHistory,
    updateLocation,
  };
};

export default useDriver;
